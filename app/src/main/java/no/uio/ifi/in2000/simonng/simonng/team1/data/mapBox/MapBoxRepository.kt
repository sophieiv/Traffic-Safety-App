package no.uio.ifi.in2000.simonng.simonng.team1.data.mapBox

import android.content.Context
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.CameraOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker

interface MapBoxRepository {
    fun hentStandardKartStil(): String
    fun hentStandardKamera(): CameraOptions
    fun hentFylkeKameraInnstillinger(fylke: Fylker): CameraOptions
    fun hentFylkeGeoJson(): Flow<Result<FeatureCollection>>
    fun hentFylkeFarger(): Map<String, String>
    fun hentFylkeLagInnstillinger(): Map<String, Any>
    fun hentBrukerLokasjon(): Pair<Double, Double>?
}

class MapBoxRepositoryImpl(private val dataSource: MapBoxDataSource) : MapBoxRepository {

    override fun hentStandardKartStil(): String {
        return dataSource.hentStandardKartStil()
    }

    override fun hentStandardKamera(): CameraOptions {
        return dataSource.hentStandardKamera()
    }

    override fun hentFylkeKameraInnstillinger(fylke: Fylker): CameraOptions {
        return dataSource.hentFylkeKameraInnstillinger(fylke)
    }

    override fun hentFylkeGeoJson(): Flow<Result<FeatureCollection>> = flow {
        emit(dataSource.hentFylkeGeoJson())
    }

    override fun hentFylkeFarger(): Map<String, String> {
        return dataSource.hentFylkeFarger()
    }

    override fun hentFylkeLagInnstillinger(): Map<String, Any> {
        return dataSource.hentFylkeLagInnstillinger()
    }

    override fun hentBrukerLokasjon(): Pair<Double, Double> {
        return dataSource.hentBrukerLokasjon()
    }

    // Sikrer at det kun eksisterer én instans av MapBoxRepository i hele appen (Singleton-mønsteret)
    companion object {
        @Volatile
        private var INSTANS: MapBoxRepository? = null

        fun hentInstans(context: Context) : MapBoxRepository {
            return INSTANS
                ?: synchronized(this) {
                val instans =
                    MapBoxRepositoryImpl(
                        MapBoxDataSource(context)
                    )
                INSTANS = instans
                instans
            }
        }
    }
}