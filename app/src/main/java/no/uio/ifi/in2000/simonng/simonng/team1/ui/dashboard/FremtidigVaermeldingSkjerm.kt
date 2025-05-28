package no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.simonng.simonng.team1.data.FylkeUtils
import no.uio.ifi.in2000.simonng.simonng.team1.ui.fylkekart.RisikoIndikatorBoks
import no.uio.ifi.in2000.simonng.simonng.team1.ui.vaer.FellesVaerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class) //Nødvendig for TopAppBar
@Composable
fun FremtidigVaermeldingSkjerm(
    navController: NavController,
    fellesVaerViewModel: FellesVaerViewModel
) {
    val uiTilstand by fellesVaerViewModel.uiTilstand.collectAsState()
    val forecastList = uiTilstand.utvidetVarsel


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("10-dagers varsel for ${uiTilstand.valgtFylke.visningsnavn}", color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbake",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        when {
            uiTilstand.laster && forecastList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiTilstand.feilmelding != null && forecastList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Kunne ikke laste 10-dagers varsel:\n${uiTilstand.feilmelding}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            forecastList.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp, start = 20.dp, end = 16.dp)
                        ) {
                            Text(
                                "Dato",
                                modifier = Modifier.weight(1.5f),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Text(
                                "Vær",
                                modifier = Modifier.weight(2f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Text(
                                "Risiko",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    }

                    items(forecastList, key = { it.dato }) { dag ->
                        FullDagVarselElement(dag = dag)
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Ingen 10-dagers varsel tilgjengelig.",
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun FullDagVarselElement(
    dag: DagligVarselData,
    modifier: Modifier = Modifier
) {
    val inputFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale("no", "NO")) }
    val outputFormat = remember { SimpleDateFormat("d. MMM", Locale("no", "NO")) }
    val formatertDato = remember(dag.dato) {
        try {
            inputFormat.parse(dag.dato)?.let { outputFormat.format(it) } ?: dag.dato
        } catch (e: Exception) {
            dag.dato
        }
    }

    val risikoTekst = when (dag.risikoNivaa.uppercase()) {
        "MOD" -> FylkeUtils.RISIKO_MODERAT
        "ØKT" -> FylkeUtils.RISIKO_OEKT
        "HØY" -> FylkeUtils.RISIKO_HOEY
        else -> FylkeUtils.RISIKO_UKJENT
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatertDato,
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Image(
                painter = painterResource(id = dag.ikonResource),
                contentDescription = "Værikon for $formatertDato",
                modifier = Modifier.size(32.dp)
            )
            Box(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = "  ${dag.maxTemp}°/${dag.minTemp}°",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(8.dp))

            RisikoIndikatorBoks(risikoNivaa = risikoTekst, tekstFarge = Color.Black)
        }
    }
}