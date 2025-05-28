package no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.simonng.simonng.team1.data.BrukerInfo
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import no.uio.ifi.in2000.simonng.simonng.team1.data.ki.AzureOkHttpClient
import no.uio.ifi.in2000.simonng.simonng.team1.ui.vaer.FellesVaerUITilstand

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "DashboardViewModel"
        private const val AI_REFRESH_INTERVAL_MS = 60L * 60 * 1000  // 1 time (60 minutter)
        private const val CACHE_VALIDITY_MS = 2L * 60 * 60 * 1000   // 2 timer

        const val RISK_MOD = "MOD"
        const val RISK_UKJENT = "Ukjent"

        val SUPPORTED_FYLKER: List<Fylker> = Fylker.entries
    }

    private data class CachedData<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isValid(maxAgeMs: Long = CACHE_VALIDITY_MS): Boolean {
            return System.currentTimeMillis() - timestamp < maxAgeMs
        }
    }

    private val brukerInfo = BrukerInfo(application.applicationContext)
    private val azureClient = AzureOkHttpClient()
    private val promptBuilder = PromptBuilder()

    private val nettverkManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var erNettverkTilgjengelig = MutableStateFlow(sjekkOmNettverkErTilkoblet())
    private var ventendeDatOppdatering = false

    private val _uiState = MutableStateFlow(DashboardUiState(selectedFylke = SUPPORTED_FYLKER.first()))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val aiTipsCache = mutableMapOf<String, CachedData<String>>()
    private val riskExplanationCache = mutableMapOf<String, CachedData<String>>()

    // Map som holder styr på når tips ble hentet sist for hvert fylke
    private val lastAITipsTimestamp = mutableMapOf<Fylker, Long>()

    private var weatherJob: Job? = null
    private var aiTipsJob: Job? = null
    private var riskExplanationJob: Job? = null


    private val nettverkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Nettverkstilkobling er tilgjengelig")
            erNettverkTilgjengelig.value = true
            if (ventendeDatOppdatering) {
                ventendeDatOppdatering = false
                _uiState.update {
                    it.copy(
                        errorMessage = null,
                        isLoading = false
                    )
                }
                oppdaterDataEtterGjenopprettetTilkobling()
            }
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Nettverkstilkobling er tapt")
            erNettverkTilgjengelig.value = false
            ventendeDatOppdatering = true
        }
    }

    init {
        viewModelScope.launch {
            initializeData()
        }

        registrerNettverkCallback()
    }

    private fun registrerNettverkCallback() {
        try {
            val nettverkForespoersel = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            nettverkManager.registerNetworkCallback(nettverkForespoersel, nettverkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Kunne ikke registrere nettverkscallback: ${e.message}")
        }
    }

    private fun avregistrerNettverkCallback() {
        try {
            nettverkManager.unregisterNetworkCallback(nettverkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Kunne ikke avregistrere nettverkscallback: ${e.message}")
        }
    }

    private fun sjekkOmNettverkErTilkoblet(): Boolean {
        val nettverk = nettverkManager.activeNetwork
        val egenskaper = nettverkManager.getNetworkCapabilities(nettverk)
        return egenskaper?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun oppdaterDataEtterGjenopprettetTilkobling() {
        val naaVaerendeFylke = _uiState.value.selectedFylke
        selectFylke(naaVaerendeFylke)
    }

    private suspend fun initializeData() {
        try {
            val lagretFylke = brukerInfo.valgtFylkeFlow.first()
            selectFylke(lagretFylke)
        } catch (e: Exception) {
            Log.e(TAG, "Feil ved initialisering: ${e.message}")
            selectFylke(SUPPORTED_FYLKER.first())
        }
    }

    fun selectFylke(newFylke: Fylker) {
        if (newFylke != _uiState.value.selectedFylke && newFylke in SUPPORTED_FYLKER) {
            viewModelScope.launch {
                brukerInfo.lagreValgtFylke(newFylke)
            }
            _uiState.update { it.copy(selectedFylke = newFylke) }
        }
    }

    fun updateFromFellesVaerState(fellesState: FellesVaerUITilstand) {
        _uiState.update { currentState ->
            if (currentState.selectedFylke == fellesState.valgtFylke) {
                currentState.copy(
                    temperaturTekst = fellesState.temperaturTekst.takeIf { it.isNotEmpty() } ?: currentState.temperaturTekst,
                    fetchedSymbolCode = fellesState.hentetSymbolKode ?: currentState.fetchedSymbolCode,
                    fetchedTempForPrompt = fellesState.hentetTempForPrompt ?: currentState.fetchedTempForPrompt,
                    fetchedNedborForPrompt = fellesState.hentetNedborForPrompt ?: currentState.fetchedNedborForPrompt,
                    calculatedRisk = fellesState.beregnetRisiko ?: currentState.calculatedRisk,
                    errorMessage = null,
                    isLoading = false
                )
            } else {
                val fylkeNavn = hentFylkeNavn(currentState.selectedFylke)
                val risikoNivaa = fellesState.alleFylkeRisikoer[fylkeNavn]

                currentState.copy(
                    calculatedRisk = risikoNivaa ?: currentState.calculatedRisk,
                    isLoading = risikoNivaa == null
                )
            }
        }

        val currentState = _uiState.value
        if (currentState.fetchedTempForPrompt != null &&
            currentState.fetchedNedborForPrompt != null &&
            currentState.calculatedRisk != null) {

            val selectedFylke = currentState.selectedFylke
            val shouldFetchNewTips = currentState.currentTipsFylke != selectedFylke ||
                    skalOppdatereAITips(selectedFylke) ||
                    currentState.aiTips == null

            if (shouldFetchNewTips) {
                // Først sjekk om vi har en gyldig cache for dette fylket
                val cachedTips = finnCachetTipsForFylke(selectedFylke)

                if (cachedTips != null) {
                    // Bruk cache hvis den finnes
                    _uiState.update { it.copy(
                        aiTips = cachedTips,
                        currentTipsFylke = selectedFylke
                    ) }
                } else {
                    // Hent nye tips fra API hvis vi ikke har cache
                    hentAiTipsAsynkront(
                        selectedFylke,
                        currentState.fetchedTempForPrompt,
                        currentState.fetchedNedborForPrompt,
                        currentState.calculatedRisk
                    )
                }
            }
        }
    }

    private fun skalOppdatereAITips(fylke: Fylker): Boolean {
        val lastRefresh = lastAITipsTimestamp[fylke] ?: 0L
        return System.currentTimeMillis() - lastRefresh > AI_REFRESH_INTERVAL_MS
    }

    private fun finnCachetTipsForFylke(fylke: Fylker): String? {
        val fylkeNavn = hentFylkeNavn(fylke)
        val risikoNivaa = _uiState.value.calculatedRisk ?: return null

        val cacheNokkel = "$fylkeNavn-$risikoNivaa-tips"
        val cachedData = aiTipsCache[cacheNokkel]

        return if (cachedData != null && cachedData.isValid(AI_REFRESH_INTERVAL_MS)) {
            // Hvis cachet data er gyldig, oppdater ikke timestamp så vi beholder original cache tid
            cachedData.data
        } else {
            null
        }
    }

    private fun hentAiTipsAsynkront(
        fylke: Fylker,
        temperatur: Float,
        nedbor: Float,
        risikoNivaa: String
    ) {
        aiTipsJob?.cancel()

        aiTipsJob = startSikkertJob(
            scope = viewModelScope,
            vedFeil = { e ->
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e(TAG, "Feil ved henting av AI tips: ${e.message}")
                    // Bruk cached data som fallback
                    proevAABrukeLagretAiTips(fylke)
                }
            }
        ) {
            try {
                val fylkeNavn = hentFylkeNavn(fylke)
                val prompt = promptBuilder.byggTipsPrompt(
                    temperatur = temperatur,
                    nedbor = nedbor,
                )

                val tips = azureClient.askAssistant(prompt)
                if (tips.startsWith("Feil:", ignoreCase = true)) {
                    throw Exception(tips)
                }

                val rensetTips = tips.trim()

                // Oppdaterer cache
                val cacheNokkel = "$fylkeNavn-$risikoNivaa-tips"
                aiTipsCache[cacheNokkel] = CachedData(rensetTips)
                lastAITipsTimestamp[fylke] = System.currentTimeMillis()

                // Oppdaterer UI
                _uiState.update { it.copy(
                    aiTips = rensetTips,
                    currentTipsFylke = fylke
                ) }

                Log.d(TAG, "Nye tips for $fylkeNavn hentet og cachet")

            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Feil ved henting av AI tips: ${e.message}")
                proevAABrukeLagretAiTips(fylke)
            }
        }
    }

    //Tips-fallback dersom vi har tips for et annet fylke (f.eks. pga. nettverksproblemer)
    private fun proevAABrukeLagretAiTips(fylke: Fylker) {
        val cachedTips = finnCachetTipsForFylke(fylke)

        if (cachedTips != null) {
            _uiState.update { it.copy(
                aiTips = cachedTips,
                currentTipsFylke = fylke
            ) }
        } else {
            val currentRisk = _uiState.value.calculatedRisk ?: return

            for ((cacheKey, cached) in aiTipsCache) {
                if (cacheKey.contains("-$currentRisk-") && cached.isValid(AI_REFRESH_INTERVAL_MS)) {
                    _uiState.update { it.copy(
                        aiTips = cached.data,
                        currentTipsFylke = fylke
                    ) }
                    Log.d(TAG, "Bruker fallback-tips fra annet fylke for $fylke")
                    break
                }
            }
        }
    }

    fun fetchRiskExplanation() {
        val currentState = _uiState.value
        val currentFylke = currentState.selectedFylke
        val risiko = currentState.calculatedRisk ?: RISK_UKJENT
        val fylkeNavn = hentFylkeNavn(currentFylke)

        // Sjekker om vi har forklaringen i cache
        val cacheNokkel = "$fylkeNavn-$risiko-explanation"
        val cachedData = riskExplanationCache[cacheNokkel]

        if (cachedData != null && cachedData.isValid()) {
            _uiState.update { it.copy(
                riskExplanation = cachedData.data,
                isRiskExplanationLoading = false,
                riskExplanationError = null
            ) }
            return
        }

        // Håndterer standard nivåer direkte
        if (risiko == RISK_UKJENT || risiko == RISK_MOD) {
            val standardForklaring = when(risiko) {
                RISK_MOD -> "Forholdene er vurdert som moderate i $fylkeNavn. Følg generelle forhåndsregler."
                else -> "Ingen spesielle værrelaterte farer utover det vanlige forventes i $fylkeNavn."
            }

            riskExplanationCache[cacheNokkel] = CachedData(standardForklaring)
            _uiState.update { it.copy(
                riskExplanation = standardForklaring,
                isRiskExplanationLoading = false,
                riskExplanationError = null
            ) }
            return
        }

        // Sjekker om vi har nødvendige data
        if (currentState.fetchedTempForPrompt == null || currentState.fetchedNedborForPrompt == null) {
            _uiState.update { it.copy(
                isRiskExplanationLoading = false,
                riskExplanationError = "Mangler værdata for å hente forklaring."
            ) }
            return
        }

        // Henter fra API
        if (erNettverkTilgjengelig.value) {
            hentRisikoForklaringFraAPI(
                currentFylke,
                risiko,
                currentState.fetchedTempForPrompt,
                currentState.fetchedNedborForPrompt
            )
        } else {
            _uiState.update { it.copy(
                isRiskExplanationLoading = false,
                riskExplanationError = "Ingen internettforbindelse. Kunne ikke hente forklaring."
            ) }
            ventendeDatOppdatering = true
        }
    }

    private fun hentRisikoForklaringFraAPI(
        fylke: Fylker,
        risiko: String,
        temperatur: Float,
        nedbor: Float
    ) {
        riskExplanationJob?.cancel()

        riskExplanationJob = startSikkertJob(viewModelScope) {
            _uiState.update { it.copy(isRiskExplanationLoading = true, riskExplanationError = null) }

            try {
                val fylkeNavn = hentFylkeNavn(fylke)
                val prompt = promptBuilder.byggRisikoForklaringPrompt(
                    fylke = fylkeNavn,
                    risiko = risiko,
                    temperatur = temperatur,
                    nedbor = nedbor
                )

                val explanation = azureClient.askAssistant(prompt)
                if (!explanation.startsWith("Feil:", ignoreCase = true)) {
                    val cleanExplanation = explanation.trim()
                    val cacheKey = "$fylkeNavn-$risiko-explanation"

                    riskExplanationCache[cacheKey] = CachedData(cleanExplanation)
                    _uiState.update { it.copy(
                        riskExplanation = cleanExplanation,
                        isRiskExplanationLoading = false,
                        riskExplanationError = null
                    ) }
                } else {
                    throw Exception(explanation)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kunne ikke hente risikoforklaring: ${e.message}")
                _uiState.update { it.copy(
                    isRiskExplanationLoading = false,
                    riskExplanationError = e.message ?: "Kunne ikke hente forklaring"
                ) }
            }
        }
    }

    private fun startSikkertJob(
        scope: CoroutineScope,
        vedFeil: ((Exception) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return scope.launch {
            try {
                block()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Feil i coroutine: ${e.message}", e)
                vedFeil?.invoke(e)
            }
        }
    }

    private fun cancelJobs() {
        weatherJob?.cancel()
        aiTipsJob?.cancel()
        riskExplanationJob?.cancel()
    }

    fun hentFylkeNavn(fylke: Fylker): String {
        return fylke.visningsnavn
    }

    override fun onCleared() {
        super.onCleared()
        cancelJobs()
        avregistrerNettverkCallback()
    }
}