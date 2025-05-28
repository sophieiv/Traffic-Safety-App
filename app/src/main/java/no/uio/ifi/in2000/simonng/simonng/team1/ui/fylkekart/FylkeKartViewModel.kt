package no.uio.ifi.in2000.simonng.simonng.team1.ui.fylkekart

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.CameraOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.simonng.simonng.team1.R
import no.uio.ifi.in2000.simonng.simonng.team1.data.FylkeUtils
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import no.uio.ifi.in2000.simonng.simonng.team1.data.mapBox.MapBoxRepository
import no.uio.ifi.in2000.simonng.simonng.team1.data.mapBox.MapBoxRepositoryImpl
import no.uio.ifi.in2000.simonng.simonng.team1.ui.vaer.FellesVaerViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

class FylkeKartViewModel(
    kontekst: Context,
    private val fellesVaerViewModel: FellesVaerViewModel
) : ViewModel() {

    private val _naavaerendeFylke = MutableStateFlow(Fylker.OSLO)
    val naavaerendeFylke: StateFlow<Fylker> = _naavaerendeFylke.asStateFlow()

    private val _fylkeData = MutableStateFlow<FeatureCollection?>(null)
    val fylkeData: StateFlow<FeatureCollection?> = _fylkeData.asStateFlow()

    private val _lasterData = MutableStateFlow(true)
    val lasterData: StateFlow<Boolean> = _lasterData.asStateFlow()

    private val mapBoxRepository: MapBoxRepository = MapBoxRepositoryImpl.hentInstans(kontekst)

    private val _kameraInnstillinger = MutableStateFlow(mapBoxRepository.hentStandardKamera())
    val kameraInnstillinger: StateFlow<CameraOptions> = _kameraInnstillinger.asStateFlow()

    private val _fylkeFarger = MutableStateFlow(mapBoxRepository.hentFylkeFarger())
    val fylkeFarger: StateFlow<Map<String, String>> = _fylkeFarger.asStateFlow()

    private val _fylkeLagInnstillinger = MutableStateFlow(mapBoxRepository.hentFylkeLagInnstillinger())
    val fylkeLagInnstillinger: StateFlow<Map<String, Any>> = _fylkeLagInnstillinger.asStateFlow()

    private val _kartStil = MutableStateFlow(mapBoxRepository.hentStandardKartStil())
    val kartStil: StateFlow<String> = _kartStil.asStateFlow()

    private val _fylkeRisikoNivaa = MutableStateFlow<Map<String, String>>(emptyMap())
    val fylkeRisikoNivaa: StateFlow<Map<String, String>> = _fylkeRisikoNivaa.asStateFlow()

    private var erDataLastet = false
    private var erRisikoDataLastet = false
    private val nettverkManager = kontekst.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var erNettverkTilgjengelig = sjekkOmNettverkErTilkoblet()

    companion object {
        private const val TAG = "FylkeKartViewModel"
    }

    init {
        registrerNettverkCallback()
        lastFylkeData(kontekst)
        hentRisikoDataEnGang()
    }

    private fun registrerNettverkCallback() {
        val nettverkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                erNettverkTilgjengelig = true
                if (!erRisikoDataLastet) {
                    hentRisikoDataEnGang()
                }
            }

            override fun onLost(network: Network) {
                erNettverkTilgjengelig = false
            }
        }

        try {
            val nettverkForespoersel = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            nettverkManager.registerNetworkCallback(nettverkForespoersel, nettverkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Kunne ikke registrere nettverkscallback: ${e.message}")
        }
    }

    private fun sjekkOmNettverkErTilkoblet(): Boolean {
        val nettverk = nettverkManager.activeNetwork
        val egenskaper = nettverkManager.getNetworkCapabilities(nettverk)
        return egenskaper?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun lastFylkeData(kontekst: Context) {
        if (erDataLastet && _fylkeData.value != null) return

        viewModelScope.launch {
            _lasterData.value = true

            try {
                val inputStream = kontekst.resources.openRawResource(R.raw.norge_fylker)
                val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                val featureCollection = FeatureCollection.fromJson(jsonString)

                _fylkeData.value = featureCollection
                erDataLastet = true

                if (!erRisikoDataLastet) {
                    hentRisikoDataEnGang()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Feil ved lasting av fylkedata: ${e.message}")
                _fylkeData.value = null
                erDataLastet = false
            } finally {
                _lasterData.value = false
            }
        }
    }

    private fun hentRisikoDataEnGang() {
        if (erRisikoDataLastet) return

        viewModelScope.launch {
            val risikoNivaaMap = mutableMapOf<String, String>()
            Fylker.entries.forEach { fylke ->
                risikoNivaaMap[fylke.visningsnavn] = FylkeUtils.RISIKO_LASTER
            }
            _fylkeRisikoNivaa.value = risikoNivaaMap

            // Venter for FellesVaerViewModell
            var forsok = 0
            val maksAntallForsok = 10
            var risikoerFraFellesVM: Map<String, String>

            while (forsok < maksAntallForsok) {
                risikoerFraFellesVM = fellesVaerViewModel.hentAlleRisikoNivaaer()

                if (risikoerFraFellesVM.isNotEmpty() &&
                    risikoerFraFellesVM.values.none { it == FylkeUtils.RISIKO_LASTER }) {
                    _fylkeRisikoNivaa.value = risikoerFraFellesVM
                    erRisikoDataLastet = true
                    Log.d(TAG, "Hentet risiko fra FellesVaerViewModel én gang: ${risikoerFraFellesVM.size} fylker")
                    return@launch
                }

                //Venter før neste forsøk
                kotlinx.coroutines.delay(1000)
                forsok++
            }

            // Ved internettfeil - bruker allerede tilgjengelig data for å vise noe annet enn grå fylker
            risikoerFraFellesVM = fellesVaerViewModel.hentAlleRisikoNivaaer()
            if (risikoerFraFellesVM.isNotEmpty()) {
                _fylkeRisikoNivaa.value = risikoerFraFellesVM
                erRisikoDataLastet = true
                Log.d(TAG, "Hentet risiko fra FellesVaerViewModel etter $forsok forsøk: ${risikoerFraFellesVM.size} fylker")
            } else {
                Log.w(TAG, "Kunne ikke hente fullstendig risiko-data etter $maksAntallForsok forsøk")
            }
        }
    }

    fun oppdaterValgtFylke(fylke: Fylker) {
        _naavaerendeFylke.value = fylke
        _kameraInnstillinger.value = mapBoxRepository.hentFylkeKameraInnstillinger(fylke)
    }

    override fun onCleared() {
        super.onCleared()

        try {
            nettverkManager.unregisterNetworkCallback(object : ConnectivityManager.NetworkCallback() {})
        } catch (e: Exception) {
            Log.e(TAG, "Feil ved onCleared: ${e.message}")
        }
    }

    class Factory(
        private val kontekst: Context,
        private val fellesVaerViewModel: FellesVaerViewModel
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FylkeKartViewModel::class.java)) {
                return FylkeKartViewModel(kontekst, fellesVaerViewModel) as T
            }
            throw IllegalArgumentException("Ukjent ViewModel-klasse")
        }
    }
}