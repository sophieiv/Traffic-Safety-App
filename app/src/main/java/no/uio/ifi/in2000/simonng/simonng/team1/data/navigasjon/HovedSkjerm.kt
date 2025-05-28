package no.uio.ifi.in2000.simonng.simonng.team1.data.navigasjon

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import no.uio.ifi.in2000.simonng.simonng.team1.R
import no.uio.ifi.in2000.simonng.simonng.team1.ui.info.BrukerViewModel
import no.uio.ifi.in2000.simonng.simonng.team1.ui.theme.MorkeBlaa
import no.uio.ifi.in2000.simonng.simonng.team1.data.onBoarding.OnBoardingRepository
import no.uio.ifi.in2000.simonng.simonng.team1.ui.onBoarding.OnboardingEntryPoint
import no.uio.ifi.in2000.simonng.simonng.team1.ui.info.OmTryggveSkjerm
import no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard.DashboardSkjerm
import no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard.DashboardViewModel
import no.uio.ifi.in2000.simonng.simonng.team1.ui.profil.ProfilScreen
import no.uio.ifi.in2000.simonng.simonng.team1.ui.fylkekart.FylkeKartSkjerm
import no.uio.ifi.in2000.simonng.simonng.team1.ui.fylkekart.FylkeKartViewModel
import no.uio.ifi.in2000.simonng.simonng.team1.ui.hjelp.HjelpSkjerm
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import no.uio.ifi.in2000.simonng.simonng.team1.ui.SystemBarsKonfig
import no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard.FremtidigVaermeldingSkjerm
import no.uio.ifi.in2000.simonng.simonng.team1.ui.hjelp.UlykkeInfoSkjerm
import no.uio.ifi.in2000.simonng.simonng.team1.ui.hjelp.VeihjelpSkjerm
import no.uio.ifi.in2000.simonng.simonng.team1.ui.vaer.FellesVaerViewModel
import no.uio.ifi.in2000.simonng.simonng.team1.ui.vaer.FellesVaerViewModelFactory
import no.uio.ifi.in2000.simonng.simonng.team1.ui.theme.Team1Theme

@Composable
fun HovedSkjerm(
    brukerViewModel: BrukerViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val morkModus by brukerViewModel.morkModus.observeAsState(initial = false)

    // Instansierer DashboardViewModel på toppnivå
    val dashboardViewModel: DashboardViewModel = viewModel()

    // Oppretter en delt instans av FellesVaerViewModel
    val fellesVaerViewModel: FellesVaerViewModel = viewModel(
        factory = FellesVaerViewModelFactory(application)
    )

    // Starter innhenting av værdata for alle fylker ved oppstart
    LaunchedEffect(Unit) {
        Fylker.entries.forEach { fylke ->
            fellesVaerViewModel.hentVaerOgRisiko(fylke, fylke.visningsnavn)
        }
    }

    val fylkeKartViewModel: FylkeKartViewModel = viewModel(
        factory = FylkeKartViewModel.Factory(
            context,
            fellesVaerViewModel
        )
    )

    LaunchedEffect(Unit) {
        fylkeKartViewModel.lastFylkeData(context)
    }

    val onboardingRepository = remember { OnBoardingRepository.getInstance(context) }
    val visOnboarding by onboardingRepository.onboardingStatusFlow.collectAsState(initial = false)

    SystemBarsKonfig(
        statusBarColor = MorkeBlaa,
        darkStatusBarIcons = false,
        hideNavigationBar = true
    )

    Team1Theme(darkTheme = morkModus) {
        val gjeldeneRute = navController.currentBackStackEntryAsState().value?.destination?.route
        val visBunnmeny = gjeldeneRute != Skjerm.Onboarding.rute

        Scaffold(
            bottomBar = {
                if (visBunnmeny) {
                    BunnNavigasjonsBar(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (visOnboarding) Skjerm.Onboarding.rute else Skjerm.Dashboard.rute,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Skjerm.Onboarding.rute) {
                    OnboardingEntryPoint(navController)
                }
                composable(Skjerm.Dashboard.rute) {
                    DashboardSkjerm(
                        navController = navController,
                        dashboardViewModel = dashboardViewModel,
                        brukerViewModel = brukerViewModel,
                        fellesVaerViewModel = fellesVaerViewModel
                    )
                }
                composable(Skjerm.Kart.rute) {
                    FylkeKartSkjerm(
                        fylkeKartViewModel = fylkeKartViewModel
                    )
                }
                composable(Skjerm.Hjelp.rute) {
                    HjelpSkjerm(navController = navController)
                }
                composable(Skjerm.Profil.rute) {
                    ProfilScreen(
                        navController = navController,
                        brukerViewModel = brukerViewModel,
                        onKiModellClick = { navController.navigate(Skjerm.OmML.rute) },
                        isDarkMode = morkModus,
                        onToggleDarkMode = { brukerViewModel.lagreMorkModus(it) }
                    )
                }
                composable(Skjerm.OmML.rute) {
                    OmTryggveSkjerm(navController = navController)
                }
                composable(Skjerm.FremtidigVaermelding.rute) {
                    FremtidigVaermeldingSkjerm(
                        navController = navController,
                        fellesVaerViewModel = fellesVaerViewModel
                    )
                }
                composable(Skjerm.UlykkeInfo.rute) {
                    UlykkeInfoSkjerm(navController = navController)
                }
                composable(Skjerm.VeihjelpNumre.rute) {
                    VeihjelpSkjerm(navController = navController)
                }
            }
        }
    }
}

@Composable
fun BunnNavigasjonsBar(navController: NavController) {
    val elementer = listOf(
        BunnMenyElement(
            rute = Skjerm.Dashboard.rute,
            aktivIkon = R.drawable.hjem_aktiv,
            inaktivIkon = R.drawable.hjem_inaktiv,
            navn = "Hjem"
        ),
        BunnMenyElement(
            rute = Skjerm.Kart.rute,
            aktivIkon = R.drawable.kart_aktiv,
            inaktivIkon = R.drawable.kart_inaktiv,
            navn = "Kart"
        ),
        BunnMenyElement(
            rute = Skjerm.Hjelp.rute,
            aktivIkon = R.drawable.hjelp_aktiv,
            inaktivIkon = R.drawable.hjelp_inaktiv,
            navn = "Nødhjelp"
        ),
        BunnMenyElement(
            rute = Skjerm.Profil.rute,
            aktivIkon = R.drawable.person_aktiv,
            inaktivIkon = R.drawable.person_inaktiv,
            navn = "Profil"
        )
    )

    val gjeldeneRute = navController.currentBackStackEntryAsState().value?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .background(MorkeBlaa),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        elementer.forEach { element ->
            val valgt = gjeldeneRute == element.rute
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        navController.navigate(element.rute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (valgt) element.aktivIkon else element.inaktivIkon
                        ),
                        contentDescription = element.navn,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = element.navn,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (valgt) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}