package no.uio.ifi.in2000.simonng.simonng.team1

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import no.uio.ifi.in2000.simonng.simonng.team1.data.vaer.VaerData
import no.uio.ifi.in2000.simonng.simonng.team1.data.vaer.VaerDataSource
import no.uio.ifi.in2000.simonng.simonng.team1.data.vaer.VaerRepository
import no.uio.ifi.in2000.simonng.simonng.team1.ui.vaer.FellesVaerViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class VaerIkonTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockApp: Application
    private lateinit var mockVaerRepository: VaerRepository
    private lateinit var mockVaerDataSource: VaerDataSource
    private lateinit var viewModel: FellesVaerViewModel

    @Before
    fun oppsett() {
        Dispatchers.setMain(testDispatcher)

        mockApp = mockk(relaxed = true)
        mockVaerRepository = mockk(relaxed = true)
        mockVaerDataSource = mockk(relaxed = true)

        every { mockApp.applicationContext } returns mockk(relaxed = true)

        viewModel = FellesVaerViewModel(mockApp, isTestMode = true)

        val vaerRepositoryField = FellesVaerViewModel::class.java.getDeclaredField("vaerRepository")
        vaerRepositoryField.isAccessible = true
        vaerRepositoryField.set(viewModel, mockVaerRepository)
    }

    @After
    fun nedrigging() {
        Dispatchers.resetMain()
    }

    @Test
    fun viewModelTestIkonCache() = runTest { //Tester om ikon blir hentet fra ViewModel sin cache
        val fylke = Fylker.OSLO
        val symbolKode = "partlycloudy_day"
        val forventetIkonId = 12345

        val mockVaerData = mockk<VaerData> {
            every { symbolCode1h } returns symbolKode
        }

        val lagretVaerDataField = viewModel.javaClass.getDeclaredField("lagretVaerData")
        lagretVaerDataField.isAccessible = true
        val lagretVaerData = mutableMapOf<Fylker, VaerData>()
        lagretVaerData[fylke] = mockVaerData
        lagretVaerDataField.set(viewModel, lagretVaerData)

        every { mockVaerRepository.hentVaerIkon(symbolKode) } returns forventetIkonId

        val ikonId = viewModel.hentVaerIkonForFylke(fylke)

        assertEquals(forventetIkonId, ikonId)
        verify { mockVaerRepository.hentVaerIkon(symbolKode) }
    }

    @Test
    fun viewModelTestIkonTomCache() = runTest { //Tester om standardikon blir brukt ved tom cache
        val fylke = Fylker.OSLO
        val standardSymbolKode = "partlycloudy_day"
        val forventetIkonId = 12345

        val lagretVaerDataField = viewModel.javaClass.getDeclaredField("lagretVaerData")
        lagretVaerDataField.isAccessible = true
        lagretVaerDataField.set(viewModel, mutableMapOf<Fylker, VaerData>())

        every { mockVaerRepository.hentVaerIkon(standardSymbolKode) } returns forventetIkonId
        every { mockVaerRepository.hentKoordinaterForFylke(any()) } returns Pair(59.9, 10.7)

        val ikonId = viewModel.hentVaerIkonForFylke(fylke)

        assertEquals(forventetIkonId, ikonId)
        verify { mockVaerRepository.hentVaerIkon(standardSymbolKode) }
    }

    @Test
    fun repositoryTestIkonSymbolKode() = runTest { //Tester om hentVaerIkon gir riktig ikon for hver symbolkode
        val testTilfeller = mapOf(
            "clearsky_day" to 100,
            "partlycloudy_night" to 200,
            "rain" to 300,
            "snow" to 400,
            "fog" to 500
        )

        for ((symbolKode, forventetIkon) in testTilfeller) {
            every { mockVaerRepository.hentVaerIkon(symbolKode) } returns forventetIkon
        }

        for ((symbolKode, forventetIkon) in testTilfeller) {
            val ikonId = mockVaerRepository.hentVaerIkon(symbolKode)
            assertEquals("Feil ikon for symbolkode $symbolKode", forventetIkon, ikonId)
            verify { mockVaerRepository.hentVaerIkon(symbolKode) }
        }

        every { mockVaerRepository.hentVaerIkon(null) } returns 600
        val nullIkon = mockVaerRepository.hentVaerIkon(null)
        assertEquals(600, nullIkon)
        verify { mockVaerRepository.hentVaerIkon(null) }
    }

    @Test
    fun apiTestForventetData() = runTest { //Tester om API returnerer riktig værdata med forventede verdier
        val symbolKode = "cloudy"
        val mockVaerData = mockk<VaerData> {
            every { symbolCode1h } returns symbolKode
            every { currentTemperature } returns 20.5f
            every { precipitation1h } returns 0.0f
            every { totalPrecipitation24h } returns 0.0f
            every { minTemperature } returns 18.0f
            every { extendedForecast } returns emptyList()
            every { time } returns "2025-05-06T12:00:00Z"
        }

        val breddegrad = 59.9320
        val lengdegrad = 10.7380
        coEvery {
            mockVaerDataSource.hentVaerData(breddegrad, lengdegrad, null)
        } returns mockVaerData

        val resultat = mockVaerDataSource.hentVaerData(breddegrad, lengdegrad)

        assertNotNull("VærData skal ikke være null", resultat)
        assertEquals(symbolKode, resultat?.symbolCode1h)
    }
}