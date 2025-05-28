package no.uio.ifi.in2000.simonng.simonng.team1

import android.content.Context
import android.content.res.Resources
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import no.uio.ifi.in2000.simonng.simonng.team1.data.mapBox.MapBoxDataSource
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapBoxDataSourceTest {

    private lateinit var mockContext: Context
    private lateinit var mockResources: Resources
    private lateinit var mapBoxDataSource: MapBoxDataSource

    @Before
    fun setup() {
        mockResources = mockk()
        mockContext = mockk()
        every { mockContext.resources } returns mockResources
        mapBoxDataSource = MapBoxDataSource(mockContext)
    }

    @Test
    fun testHentStandardKartStil() {
        val kartStil = mapBoxDataSource.hentStandardKartStil()
        assertEquals(Style.STANDARD, kartStil)
    }

    @Test
    fun testHentStandardKamera() {
        val kamera = mapBoxDataSource.hentStandardKamera()
        val center = kamera.center as Point
        assertEquals(10.7522, center.longitude(), 0.0001)
        assertEquals(59.9139, center.latitude(), 0.0001)
        assertEquals(7.5, kamera.zoom)
    }

    @Test
    fun testHentFylkeGeoJson_Success() = runBlocking {
        val validGeoJson = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": {},
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[10.0, 59.0], [11.0, 59.0], [11.0, 60.0], [10.0, 60.0], [10.0, 59.0]]]
                  },
                  "id": "03"
                }
              ]
            }
        """.trimIndent()

        val mockInputStream = ByteArrayInputStream(validGeoJson.toByteArray())
        every { mockResources.openRawResource(any()) } returns mockInputStream

        val result = mapBoxDataSource.hentFylkeGeoJson()

        assertTrue(result.isSuccess)
        val featureCollection = result.getOrNull()
        assertEquals(1, featureCollection?.features()?.size)
        assertEquals("03", featureCollection?.features()?.get(0)?.id())
    }

    @Test
    fun testHentFylkeGeoJson_Failure() = runBlocking {
        every { mockResources.openRawResource(any()) } throws java.io.IOException("Test exception")

        val result = mapBoxDataSource.hentFylkeGeoJson()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is java.io.IOException)
    }

    @Test
    fun testHentFylkeFarger() {
        val farger = mapBoxDataSource.hentFylkeFarger()

        assertEquals("#1A237E", farger["valgt_fylke"])
        assertEquals("#888888", farger["normal_fylke"])
        assertEquals("#000000", farger["kontur"])
        assertEquals("#00D000", farger["MOD"])
        assertEquals("#FFC900", farger["ØKT"])
        assertEquals("#FF0000", farger["HØY"])
    }

    @Test
    fun testHentFylkeLagInnstillinger() {
        val innstillinger = mapBoxDataSource.hentFylkeLagInnstillinger()

        assertEquals(0.5, innstillinger["fyll_opacity"])
        assertEquals(2.2, innstillinger["kontur_bredde"])
    }

    @Test
    fun testHentFylkeKameraInnstillingerOslo() {
        val kameraInnstillinger = mapBoxDataSource.hentFylkeKameraInnstillinger(Fylker.OSLO)
        val center = kameraInnstillinger.center as Point

        assertEquals(10.7522, center.longitude(), 0.0001)
        assertEquals(59.9139, center.latitude(), 0.0001)
        assertEquals(7.5, kameraInnstillinger.zoom)
    }

    @Test
    fun testHentFylkeKameraInnstillingerAkershus() {
        val kameraInnstillinger = mapBoxDataSource.hentFylkeKameraInnstillinger(Fylker.AKERSHUS)
        val center = kameraInnstillinger.center as Point

        assertEquals(11.0, center.longitude(), 0.0001)
        assertEquals(59.9, center.latitude(), 0.0001)
        assertEquals(7.5, kameraInnstillinger.zoom)
    }

    @Test
    fun testHentFylkeKameraInnstillingerBuskerud() {
        val kameraInnstillinger = mapBoxDataSource.hentFylkeKameraInnstillinger(Fylker.BUSKERUD)
        val center = kameraInnstillinger.center as Point

        assertEquals(9.5, center.longitude(), 0.0001)
        assertEquals(60.0, center.latitude(), 0.0001)
        assertEquals(7.5, kameraInnstillinger.zoom)
    }

    @Test
    fun testHentFylkeKameraInnstillingerVestfold() {
        val kameraInnstillinger = mapBoxDataSource.hentFylkeKameraInnstillinger(Fylker.VESTFOLD)
        val center = kameraInnstillinger.center as Point

        assertEquals(10.2, center.longitude(), 0.0001)
        assertEquals(59.4, center.latitude(), 0.0001)
        assertEquals(7.5, kameraInnstillinger.zoom)
    }

    @Test
    fun testHentFylkeKameraInnstillingerOestfold() {
        val kameraInnstillinger = mapBoxDataSource.hentFylkeKameraInnstillinger(Fylker.OESTFOLD)
        val center = kameraInnstillinger.center as Point

        assertEquals(11.2, center.longitude(), 0.0001)
        assertEquals(59.3, center.latitude(), 0.0001)
        assertEquals(7.5, kameraInnstillinger.zoom)
    }

    @Test
    fun testHentBrukerLokasjon() {
        val (longitude, latitude) = mapBoxDataSource.hentBrukerLokasjon()
        //Sjekker koordinater for Oslo
        assertEquals(10.7522, longitude, 0.0001)
        assertEquals(59.9139, latitude, 0.0001)
    }
}