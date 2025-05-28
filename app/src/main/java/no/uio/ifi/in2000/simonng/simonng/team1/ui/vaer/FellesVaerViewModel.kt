package no.uio.ifi.in2000.simonng.simonng.team1.ui.vaer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.simonng.simonng.team1.data.FylkeUtils
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import no.uio.ifi.in2000.simonng.simonng.team1.data.ml.MLRepository
import no.uio.ifi.in2000.simonng.simonng.team1.data.vaer.DailyForecast
import no.uio.ifi.in2000.simonng.simonng.team1.data.vaer.Resource
import no.uio.ifi.in2000.simonng.simonng.team1.data.vaer.VaerData
import no.uio.ifi.in2000.simonng.simonng.team1.data.vaer.VaerDataSource
import no.uio.ifi.in2000.simonng.simonng.team1.data.vaer.VaerRepository
import no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard.DagligVarselData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class FellesVaerViewModel(
    applikasjon: Application,
    isTestMode: Boolean = false
) : AndroidViewModel(applikasjon) {

    private companion object {
        private const val TAGG = "FellesVaerViewModel"
    }

    private val mlRepository = if (isTestMode) {
        MLRepository(null, testModus = true)
    } else {
        MLRepository(applikasjon.applicationContext)
    }

    private val vaerRepository = VaerRepository(VaerDataSource())

    private val _uiTilstand = MutableStateFlow(FellesVaerUITilstand())
    val uiTilstand: StateFlow<FellesVaerUITilstand> = _uiTilstand.asStateFlow()

    private val lagretVaerData = mutableMapOf<Fylker, VaerData>()
    private val risikoCache = ConcurrentHashMap<String, String>()

    init {
        // Laster inn data for alle fylker ved oppstart
        lastInnDataForAlleFylker()
    }

    private fun lastInnDataForAlleFylker() {
        viewModelScope.launch {
            Fylker.entries.forEach { fylke ->
                launch {
                    hentVaerOgRisiko(fylke, fylke.visningsnavn)
                }
            }
        }
    }

    fun hentVaerOgRisiko(fylke: Fylker, fylkeNavn: String) {
        viewModelScope.launch {
            _uiTilstand.update {
                it.copy(
                    laster = true,
                    feilmelding = null,
                    valgtFylke = fylke
                )
            }

            try {
                Log.d(TAGG, "Henter værdata for $fylkeNavn")

                val (breddegrad, lengdegrad) = vaerRepository.hentKoordinaterForFylke(fylkeNavn)

                val vaerResource = vaerRepository.hentVaerMelding(breddegrad, lengdegrad)
                    .filterNot { it is Resource.Loading }
                    .first()

                when (vaerResource) {
                    is Resource.Success -> {
                        val vaerData = vaerResource.data
                        lagretVaerData[fylke] = vaerData

                        val utvidetVarsel = lagUtvidetVarsel(vaerData.extendedForecast, fylkeNavn)

                        _uiTilstand.update {
                            it.copy(
                                temperaturTekst = "${vaerData.currentTemperature.toInt()}°C",
                                hentetTempForPrompt = vaerData.currentTemperature,
                                hentetNedborForPrompt = vaerData.precipitation1h,
                                hentetSymbolKode = vaerData.symbolCode1h,
                                utvidetVarsel = utvidetVarsel
                            )
                        }

                        // Henter værdata for dagens dato
                        val (nedbor, minTemp) = hentKonsistenteVaerverdierForDagensDato(vaerData)

                        val dagensDato = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                        val predikertAntall = mlRepository.predikerUlykker(
                            fylkeNavn,
                            dagensDato,
                            nedbor,
                            minTemp
                        )

                        val risikoNivaa = bestemRisikoNivaa(predikertAntall)

                        // Lagrer risikonivå i cache
                        risikoCache[fylkeNavn] = risikoNivaa

                        _uiTilstand.update {
                            val oppdatertRisikoer = it.alleFylkeRisikoer.toMutableMap()
                            oppdatertRisikoer[fylkeNavn] = risikoNivaa

                            it.copy(
                                beregnetRisiko = if (fylke == it.valgtFylke) risikoNivaa else it.beregnetRisiko,
                                laster = false,
                                hentetTempForRisiko = minTemp,
                                hentetNedborForRisiko = nedbor,
                                alleFylkeRisikoer = oppdatertRisikoer
                            )
                        }

                        Log.d(
                            TAGG,
                            "Værdata og risiko oppdatert for $fylkeNavn. Risiko: $risikoNivaa (predikert: $predikertAntall)"
                        )
                    }

                    is Resource.Error -> {
                        Log.e(TAGG, "Feil ved henting av værdata: ${vaerResource.message}")
                        _uiTilstand.update {
                            it.copy(
                                laster = false,
                                feilmelding = vaerResource.message
                            )
                        }
                    }

                    else -> {
                        Log.w(TAGG, "Uventet resource-type: $vaerResource")
                        _uiTilstand.update { it.copy(laster = false) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAGG, "Feil ved henting av data for ${fylke}: ${e.message}", e)
                _uiTilstand.update {
                    it.copy(
                        laster = false,
                        feilmelding = e.message ?: "En ukjent feil oppstod"
                    )
                }
            }
        }
    }

    fun hentAlleRisikoNivaaer(): Map<String, String> {
        return _uiTilstand.value.alleFylkeRisikoer
    }

    private fun bestemRisikoNivaa(antall: Float?): String {
        val risikoNivaa = FylkeUtils.bestemRisikoNivaa(antall)
        return risikoNivaa
    }

    private fun hentKonsistenteVaerverdierForDagensDato(vaerData: VaerData): Pair<Float, Float> {
        val datoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dagensDato = datoFormat.format(Date())

        val dagensForecast = vaerData.extendedForecast.find { it.date == dagensDato }

        return if (dagensForecast != null) {
            Log.d(TAGG, "Bruker konsistente verdier fra extendedForecast for dagens dato: " +
                    "nedbør=${dagensForecast.totalPrecipitation}, minTemp=${dagensForecast.minTemperature}")
            Pair(dagensForecast.totalPrecipitation, dagensForecast.minTemperature)
        } else {
            Log.d(TAGG, "Fant ikke dagens dato i extendedForecast, bruker verdier fra værdataen: " +
                    "nedbør=${vaerData.totalPrecipitation24h}, minTemp=${vaerData.minTemperature}")
            Pair(vaerData.totalPrecipitation24h, vaerData.minTemperature)
        }
    }

    private fun lagUtvidetVarsel(
        utvidetVarsel: List<DailyForecast>,
        fylkeNavn: String
    ): List<DagligVarselData> {
        val varselListe = mutableListOf<DagligVarselData>()

        for (dagligVarsel in utvidetVarsel) {
            try {
                val datoFormatInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val datoFormatOutputML = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                val datoDato = datoFormatInput.parse(dagligVarsel.date)
                val formatertDatoForML = datoDato?.let { datoFormatOutputML.format(it) } ?: continue

                val risiko = mlRepository.predikerUlykker(
                    fylkeNavn,
                    formatertDatoForML,
                    dagligVarsel.totalPrecipitation,
                    dagligVarsel.minTemperature
                )

                val risikoNivaa = bestemRisikoNivaa(risiko)

                val ikonResource = try {
                    vaerRepository.hentVaerIkon(dagligVarsel.symbolCode)
                } catch (e: Exception) {
                    no.uio.ifi.in2000.simonng.simonng.team1.R.drawable.partlycloudy_day
                }
                varselListe.add(
                    DagligVarselData(
                        dato = dagligVarsel.date,
                        minTemp = dagligVarsel.minTemperature.toInt(),
                        maxTemp = dagligVarsel.maxTemperature.toInt(),
                        ikonResource = ikonResource,
                        risikoNivaa = risikoNivaa
                    )
                )
            } catch (e: Exception) {
                Log.e(TAGG, "Feil i lagUtvidetVarsel for ${dagligVarsel.date}", e)
            }
        }
        return varselListe
    }

    fun hentVaerIkonForFylke(fylke: Fylker): Int {
        if (lagretVaerData.containsKey(fylke)) {
            val vaerData = lagretVaerData[fylke]!!
            return vaerRepository.hentVaerIkon(vaerData.symbolCode1h)
        }
        val fylkeNavn = hentFylkeNavn(fylke)
        viewModelScope.launch {
            hentVaerOgRisiko(fylke, fylkeNavn)
        }
        return vaerRepository.hentVaerIkon("partlycloudy_day")
    }

    private fun hentFylkeNavn(fylke: Fylker): String {
        return fylke.visningsnavn
    }

    override fun onCleared() {
        super.onCleared()
        try {
            mlRepository.lukk()
            Log.d(TAGG, "ViewModel Cleared, lukker MLRepository")
        } catch (e: Exception) {
            Log.e(TAGG, "Feil ved lukking av MLRepository", e)
        }
    }
}

data class FellesVaerUITilstand(
    val valgtFylke: Fylker = Fylker.OSLO,
    val temperaturTekst: String = "Laster...",
    val beregnetRisiko: String? = null,
    val laster: Boolean = false,
    val feilmelding: String? = null,
    val utvidetVarsel: List<DagligVarselData> = emptyList(),
    val hentetTempForPrompt: Float? = null,
    val hentetNedborForPrompt: Float? = null,
    val hentetSymbolKode: String? = null,
    val hentetTempForRisiko: Float? = null,
    val hentetNedborForRisiko: Float? = null,
    val alleFylkeRisikoer: Map<String, String> = emptyMap()
)