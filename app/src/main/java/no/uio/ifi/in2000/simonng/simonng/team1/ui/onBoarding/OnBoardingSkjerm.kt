package no.uio.ifi.in2000.simonng.simonng.team1.ui.onBoarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import no.uio.ifi.in2000.simonng.simonng.team1.data.navigasjon.Skjerm

@Composable
fun OnBoardingSkjerm(
    onboardingViewModel: OnBoardingViewModel,
    navController: NavController
) {
    val onboardingUiState by onboardingViewModel.onboardingUiState.collectAsState()
    val totalSkjermer = 5

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Hopp over",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 24.dp)
                .clickable {
                    onboardingViewModel.updateShowOnboarding(false)
                    navController.navigate(Skjerm.Dashboard.rute) {
                        popUpTo(Skjerm.Onboarding.rute) { inclusive = true }
                    }
                }
        )

        Text(
            text = "${onboardingUiState.index + 1}/$totalSkjermer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp, start = 24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (onboardingUiState.index) {
                0 -> VelkommenSkjerm()
                1 -> ForklartDashboardSkjerm()
                2 -> ForklartFylkeKartSkjerm()
                3 -> ForklartUlykkeSkjerm()
                4 -> SluttOnBoardingSkjerm()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onboardingUiState.index > 0) {
                TextButton(
                    onClick = { onboardingViewModel.gaaTilForrigeSkjerm() }
                ) {
                    Text(
                        text = "Forrige",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(totalSkjermer) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == onboardingUiState.index) 14.dp else 10.dp)
                            .background(
                                color = if (index == onboardingUiState.index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
            }

            TextButton(
                onClick = {
                    if (onboardingUiState.index == totalSkjermer - 1) {
                        onboardingViewModel.updateShowOnboarding(false)
                        navController.navigate(Skjerm.Dashboard.rute) {
                            popUpTo(Skjerm.Onboarding.rute) { inclusive = true }
                        }
                    } else {
                        onboardingViewModel.gaaTilNesteSkjerm()
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = when (onboardingUiState.index) {
                        0 -> "Jeg er med!"
                        totalSkjermer - 1 -> "Start"
                        else -> "Neste"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OnboardingEntryPoint(navController: NavController) {
    val context = LocalContext.current
    val onboardingViewModel = viewModel<OnBoardingViewModel>(
        factory = OnBoardingViewModel.Factory(context)
    )

    OnBoardingSkjerm(
        onboardingViewModel = onboardingViewModel,
        navController = navController
    )
}