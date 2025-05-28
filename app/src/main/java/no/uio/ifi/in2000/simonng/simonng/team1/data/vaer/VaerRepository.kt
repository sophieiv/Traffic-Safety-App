package no.uio.ifi.in2000.simonng.simonng.team1.data.vaer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import android.util.Log
import no.uio.ifi.in2000.simonng.simonng.team1.R


class VaerRepository(private val dataSource: VaerDataSource) {

    fun hentVaerMelding(
        breddegrad: Double,
        lengdegrad: Double,
        hoydeOverHavet: Int? = null
    ): Flow<Resource<VaerData>> = flow {
        emit(Resource.Loading())

        Log.d("VærRepository", "Henter værdata for breddegrad=$breddegrad, lengdegrad=$lengdegrad")
        val vaerData = dataSource.hentVaerData(breddegrad, lengdegrad, hoydeOverHavet)

        if (vaerData != null) {
            emit(Resource.Success(vaerData))
        } else {
            Log.e("VærRepository", "Kunne ikke hente værdata")
            emit(Resource.Error("Kunne ikke hente værdata"))
        }
    }

    fun hentVaerIkon(symbolCode: String?): Int {
        return velgVaerIkon(symbolCode)
    }

    fun hentKoordinaterForFylke(fylkeNavn: String): Pair<Double, Double> {
        return when (fylkeNavn) {
            "Oslo" -> Pair(59.9320, 10.7380)        // Oslo sentrum
            "Akershus" -> Pair(59.9400, 11.0000)     // Mer sentralt i Akershus
            "Buskerud" -> Pair(60.3370, 9.1780)     // Mer sentralt i Buskerud
            "Vestfold" -> Pair(59.3380, 10.1750)      // Mer sentralt i Vestfold
            "Østfold" -> Pair(59.3837, 11.2603)     // Mer sentralt i Østfold
            else -> Pair(59.9320, 10.7380)           // Default: Oslo
        }
    }
}

fun velgVaerIkon(symbolCode: String?): Int {
    if (symbolCode == null) return R.drawable.partlycloudy_day // Standard fallback

    return when {

        // Sludd
        symbolCode.contains("sleet") -> R.drawable.sleet

        // Snø
        symbolCode.contains("lightsnow") -> R.drawable.lightsnow
        symbolCode.contains("snow") -> R.drawable.snow
        symbolCode.contains("heavysnow") -> R.drawable.heavysnow
        symbolCode.contains("lightsnowshowers_day") -> R.drawable.lightsnowshowers_day
        symbolCode.contains("lightsnowshowers_night") -> R.drawable.lightsnowshowers_night
        symbolCode.contains("lightsnowshowers_polartwilight") -> R.drawable.lightsnowshowers_polartwilight

        // Regn
        symbolCode.contains("lightrainshowers_day") -> R.drawable.lightrainshowers_day
        symbolCode.contains("lightrainshowers_night") -> R.drawable.lightrainshowers_night
        symbolCode.contains("lightrainshowers_polartwilight") -> R.drawable.lightrainshowers_polartwilight
        symbolCode.contains("rainshowers_day") -> R.drawable.rainshowers_day
        symbolCode.contains("rainshowers_night") -> R.drawable.rainshowers_night
        symbolCode.contains("rainshowers_polartwilight") -> R.drawable.rainshowers_polartwilight
        symbolCode.contains("heavyrainshowers_day") -> R.drawable.heavyrainshowers_day
        symbolCode.contains("heavyrainshowers_night") -> R.drawable.heavyrainshowers_night
        symbolCode.contains("heavyrainshowers_polartwilight") -> R.drawable.heavyrainshowers_polartwilight
        symbolCode.contains("lightrain") -> R.drawable.lightrain
        symbolCode.contains("rain") -> R.drawable.rain
        symbolCode.contains("heavyrain") -> R.drawable.heavyrain

        // Klart
        symbolCode.contains("clearsky_day") -> R.drawable.clearsky_day
        symbolCode.contains("clearsky_night") -> R.drawable.clearsky_night
        symbolCode.contains("clearsky_polartwilight") -> R.drawable.clearsky_polartwilight

        // Lett overskyet
        symbolCode.contains("fair_day") -> R.drawable.fair_day
        symbolCode.contains("fair_night") -> R.drawable.fair_night
        symbolCode.contains("fair_polartwilight") -> R.drawable.fair_polartwilight

        // Delvis overskyet
        symbolCode.contains("partlycloudy_day") -> R.drawable.partlycloudy_day
        symbolCode.contains("partlycloudy_night") -> R.drawable.partlycloudy_night
        symbolCode.contains("partlycloudy_polartwilight") -> R.drawable.partlycloudy_polartwilight

        // Overskyet
        symbolCode.contains("cloudy") -> R.drawable.cloudy

        // Tåke
        symbolCode.contains("fog") -> R.drawable.fog

        // Standard fallback
        else -> {
            Log.d("VærIkon", "Ukjent symbolkode: $symbolCode, bruker standard ikon")
            R.drawable.partlycloudy_day
        }
    }
}

sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}