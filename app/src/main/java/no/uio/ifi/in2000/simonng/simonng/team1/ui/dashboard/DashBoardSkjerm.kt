package no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import no.uio.ifi.in2000.simonng.simonng.team1.R
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import no.uio.ifi.in2000.simonng.simonng.team1.data.navigasjon.Skjerm
import no.uio.ifi.in2000.simonng.simonng.team1.ui.NettverkBanner
import no.uio.ifi.in2000.simonng.simonng.team1.ui.info.BrukerViewModel
import no.uio.ifi.in2000.simonng.simonng.team1.ui.vaer.FellesVaerViewModel
import java.util.Locale

@Composable
fun DashboardSkjerm(
    navController: NavController,
    dashboardViewModel: DashboardViewModel,
    brukerViewModel: BrukerViewModel = viewModel(),
    fellesVaerViewModel: FellesVaerViewModel
) {
    val brukerNavn by brukerViewModel.brukerNavn.observeAsState("")
    val erForsteGang by brukerViewModel.erForsteGang.observeAsState(false)
    var visNavnDialog by remember { mutableStateOf(false) }
    val valgtFylke by brukerViewModel.valgtFylke.observeAsState(Fylker.OSLO)

    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val fellesVaerState by fellesVaerViewModel.uiTilstand.collectAsStateWithLifecycle()
    var visForklaringDialog by remember { mutableStateOf(false) }

    LaunchedEffect(erForsteGang) {
        if (erForsteGang) {
            visNavnDialog = true
        }
    }

    LaunchedEffect(valgtFylke) {
        dashboardViewModel.selectFylke(valgtFylke)
    }

    // Henter informasjon om vær og risiko for valgt fylke
    LaunchedEffect(uiState.selectedFylke) {
        val fylke = uiState.selectedFylke
        val fylkeNavn = dashboardViewModel.hentFylkeNavn(fylke)

        fellesVaerViewModel.hentVaerOgRisiko(fylke, fylkeNavn)
    }

    // Oppdaterer dashbordet når fellesVaerState endres
    LaunchedEffect(fellesVaerState) {
        dashboardViewModel.updateFromFellesVaerState(fellesVaerState)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            NettverkBanner(skjerm = "hjem")
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (brukerNavn.isNotEmpty()) "Hei, $brukerNavn" else "Værvarsel",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                FylkeVelger(
                    modifier = Modifier.padding(bottom = 16.dp),
                    naavaerendeFylke = uiState.selectedFylke,
                    onFylkeValgt = { fylke ->
                        dashboardViewModel.selectFylke(fylke)
                        brukerViewModel.lagreValgtFylke(fylke)
                    },
                    fylkeRisikoNivaa = emptyMap(),
                    visRisiko = false
                )
                // Hovedværkort
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            // Risiko
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Risiko",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val visRisiko = (uiState.calculatedRisk ?: "UKJENT").uppercase()
                                val aktivFargeIndikator = when (visRisiko) {
                                    "MOD" -> Color(0xFF00D000)
                                    "ØKT" -> Color(0xFFFFC900)
                                    "HØY" -> Color(0xFFFF0000)
                                    else -> Color(0xFF888888)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(aktivFargeIndikator, CircleShape)
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            ), CircleShape
                                        )
                                )
                                Text(
                                    text = visRisiko.lowercase()
                                        .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Vær
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Vær",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Image(
                                    painter = painterResource(id = getWeatherIconResId(uiState.fetchedSymbolCode)),
                                    contentDescription = "Værikon",
                                    modifier = Modifier.size(56.dp)
                                )
                                Text(
                                    text = uiState.temperaturTekst.takeIf { it.isNotEmpty() && it != "N/A" }
                                        ?: "--°C",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Laster/feilindikator
                        Box(
                            modifier = Modifier.height(20.dp).fillMaxWidth()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp).align(Alignment.Center),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            uiState.errorMessage?.let { feil ->
                                Text(
                                    text = "Feil: $feil",
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.align(Alignment.Center)
                                        .padding(horizontal = 16.dp)
                                )
                            }
                        }

                        // Lær mer-knapp
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    dashboardViewModel.fetchRiskExplanation()
                                    visForklaringDialog = true
                                },
                                enabled = (uiState.calculatedRisk ?: "Ukjent") != "Ukjent",
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.7f
                                    )
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.align(Alignment.BottomEnd),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Lær mer")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tips-seksjon
                Text(
                    "Ta hensyn til:",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                TipsCard(uiState = uiState)

                Spacer(modifier = Modifier.height(24.dp))

                // Navigasjonsknapper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .align(Alignment.CenterVertically),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text("Nå", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { navController.navigate(Skjerm.FremtidigVaermelding.rute) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .align(Alignment.CenterVertically),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("10 dager", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Navn-dialog (kun ved første åpning)
            if (visNavnDialog) {
                NavnInndataDialog(
                    onAvvis = {
                        brukerViewModel.markerAppSomBrukt()
                        visNavnDialog = false
                    },
                    onLagre = { navn ->
                        Log.d("DashboardScreen", "Bruker lagret navn: $navn")
                        brukerViewModel.lagreBrukerNavn(navn)
                        visNavnDialog = false
                    }
                )
            }

            // Risikoforklaringsdialog
            if (visForklaringDialog) {
                AlertDialog(
                    onDismissRequest = { visForklaringDialog = false },
                    title = {
                        Text(
                            "Hvorfor ${uiState.calculatedRisk?.replaceFirstChar { it.titlecase() } ?: "Ukjent"} risiko i ${
                                dashboardViewModel.hentFylkeNavn(
                                    uiState.selectedFylke
                                )
                            }?",
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    text = {
                        when {
                            uiState.isRiskExplanationLoading -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            uiState.riskExplanationError != null -> {
                                Text(
                                    "Kunne ikke hente forklaring: ${uiState.riskExplanationError}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            uiState.riskExplanation != null -> {
                                Text(
                                    text = uiState.riskExplanation ?: "",
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            }

                            else -> {
                                Text(
                                    "Ingen forklaring tilgjengelig.",
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { visForklaringDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) { Text("Lukk") }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    textContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun TipsCard(uiState: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically){
            Image(
                painter = painterResource(id = R.drawable.tryggve_raad),
                contentDescription = "Tryggve gir råd",
                modifier = Modifier
                    .size(70.dp)
                    .padding(top = 10.dp),
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    uiState.isLoading && uiState.aiTips == null -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    !uiState.aiTips.isNullOrBlank() -> {
                        val tipsListe = uiState.aiTips.split("\n")
                            .mapNotNull {
                                it.trim().removePrefix("→").trim().takeIf { t -> t.isNotEmpty() }
                            }
                        if (tipsListe.isNotEmpty()) {
                            tipsListe.forEach { tipsTekst ->
                                TipsElement(
                                    tittel = tipsTekst,
                                    harAdvarsel = false,
                                    textColor = MaterialTheme.colorScheme.primary,
                                )
                            }
                        } else {
                            Text(
                                "Ingen spesifikke råd tilgjengelig for øyeblikket.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                    }

                    uiState.errorMessage != null && uiState.aiTips == null -> {
                        Text(
                            "Kunne ikke laste tips: ${uiState.errorMessage}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    !uiState.isLoading && uiState.aiTips == null -> {
                        Text(
                            "Ingen tips tilgjengelig for øyeblikket.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TipsElement(
    modifier: Modifier = Modifier,
    tittel: String,
    harAdvarsel: Boolean = false,
    textColor: Color
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tittel,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }

        if (harAdvarsel) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
        }
    }
}

@Composable
private fun NavnInndataDialog(onAvvis: () -> Unit, onLagre: (String) -> Unit) {
    var navnInndata by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onAvvis,
        containerColor = MaterialTheme.colorScheme.secondary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        title = { Text("Hva ønsker du at jeg skal kalle deg?") },
        text = {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = navnInndata,
                    onValueChange = { navnInndata = it },
                    label = { Text("Navn") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.secondary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLagre(navnInndata) },
                enabled = navnInndata.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Lagre", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onAvvis) { Text("Avbryt", color = MaterialTheme.colorScheme.primary) }
        }
    )
}