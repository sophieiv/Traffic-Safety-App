package no.uio.ifi.in2000.simonng.simonng.team1.ui.info

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.simonng.simonng.team1.data.BrukerInfo
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker

class BrukerViewModel(application: Application) : AndroidViewModel(application) {

    private val brukerPreferanserManager = BrukerInfo(application.applicationContext)

    val brukerNavn = brukerPreferanserManager.brukerNavnFlow.asLiveData()

    val erForsteGang = brukerPreferanserManager.erForsteGangFlow.asLiveData()

    val morkModus: LiveData<Boolean> = brukerPreferanserManager.morkModusFlow.asLiveData()

    val valgtFylke = brukerPreferanserManager.valgtFylkeFlow.asLiveData()


    init {
        Log.d("BrukerViewModel", "ViewModel initialisert")
    }

    fun lagreBrukerNavn(navn: String) {
        viewModelScope.launch {
            Log.d("BrukerViewModel", "Lagrer brukernavn: $navn")
            brukerPreferanserManager.lagreBrukerNavn(navn)
        }
    }

    fun lagreMorkModus(aktivert: Boolean) {
        viewModelScope.launch {
            Log.d("BrukerViewModel", "Lagrer mørk modus: $aktivert")
            brukerPreferanserManager.lagreMorkModus(aktivert)
        }
    }

    fun lagreValgtFylke(fylke: Fylker) {
        viewModelScope.launch {
            Log.d("BrukerViewModel", "Lagrer valgt fylke: ${fylke.name}")
            brukerPreferanserManager.lagreValgtFylke(fylke)
        }
    }


    fun markerAppSomBrukt() {
        viewModelScope.launch {
            Log.d("BrukerViewModel", "Markerer app som brukt")
            brukerPreferanserManager.setBrukerHarIkkeBruktAppFor()
        }
    }
}