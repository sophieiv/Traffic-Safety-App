package no.uio.ifi.in2000.simonng.simonng.team1.data.mapBox

import android.content.Context
import android.util.Log
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.simonng.simonng.team1.R
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import java.io.IOException

class MapBoxDataSource (private val context: Context){

    fun hentStandardKartStil(): String {
        return Style.STANDARD
    }

    fun hentStandardKamera(): CameraOptions {
        return hentFylkeKameraInnstillinger(Fylker.OSLO)
    }

    fun hentFylkeKameraInnstillinger(fylke: Fylker): CameraOptions {
        val (koordinater, zoomNivaa) = when (fylke) {
            Fylker.OSLO -> Pair(Pair(10.7522, 59.9139), 7.5)
            Fylker.AKERSHUS -> Pair(Pair(11.0, 59.9), 7.5)
            Fylker.BUSKERUD -> Pair(Pair(9.5, 60.0), 7.5)
            Fylker.VESTFOLD -> Pair(Pair(10.2, 59.4), 7.5)
            Fylker.OESTFOLD -> Pair(Pair(11.2, 59.3), 7.5)
        }

        return CameraOptions.Builder()
            .center(Point.fromLngLat(koordinater.first, koordinater.second))
            .zoom(zoomNivaa)
            .build()
    }

    //Henter fylkedata
    suspend fun hentFylkeGeoJson(): Result<FeatureCollection> = withContext(Dispatchers.IO)  {
        try {
            val inputStream = context.resources.openRawResource(R.raw.norge_fylker)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val featureCollection = FeatureCollection.fromJson(jsonString)
            Result.success(featureCollection)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    //Henter fargekoder for fylker
    fun hentFylkeFarger(): Map<String, String> {
        return mapOf(
            "valgt_fylke" to "#1A237E", // Blå for valgt fylke
            "normal_fylke" to "#888888", // Grå for andre fylker
            "kontur" to "#000000", // Svart for fylkegrenser
            "MOD" to "#00D000",  // Grønn for moderat risiko
            "ØKT" to "#FFC900",  // Gul for økt risiko
            "HØY" to "#FF0000"   // Rød for høy risiko
        )
    }

    // Henter fylkelag-innstillinger
    fun hentFylkeLagInnstillinger(): Map<String, Any> {
        return mapOf(
            "fyll_opacity" to 0.5,
            "kontur_bredde" to 2.2
        )
    }

    // Returnerer alltid Oslo som lokasjon
    fun hentBrukerLokasjon(): Pair<Double, Double> {
        Log.d("LokasjonsDebug", "hentBrukerLokasjon ble kalt - returnerer Oslo")
        return Pair(10.7522, 59.9139)
    }
}