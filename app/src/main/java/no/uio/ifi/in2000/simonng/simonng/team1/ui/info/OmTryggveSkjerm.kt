package no.uio.ifi.in2000.simonng.simonng.team1.ui.info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.simonng.simonng.team1.ui.theme.Team1Theme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmTryggveSkjerm(navController: NavController? = null) {
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {  Text("Hvordan beregnes risiko?", color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // MERK: Illustrasjon generert ved hjelp av Claude for å kunne tilpasses skjermstørrelse og tema
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f) // Tilpasser seg skjermstørrelse
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Tegner pilene i Canvas for mer kontroll over farger
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pilTykkelse = 2.5f
                    val pilHodeLengde = 18f
                    val pilHodeVinkel = (PI / 6).toFloat()

                    val sentrum = center
                    val venstreX = size.width * 0.25f
                    val hoyreX = size.width * 0.75f
                    val vertikalAvstand = size.height * 0.22f

                    for (i in -1..1) {
                        val startX = venstreX + 50f
                        val startY = sentrum.y + i * vertikalAvstand
                        val sluttX = sentrum.x - 70f
                        val sluttY = sentrum.y + i * 20f

                        // Hovedlinjen
                        drawLine(
                            color = primaryColor,
                            start = Offset(startX, startY),
                            end = Offset(sluttX, sluttY),
                            strokeWidth = pilTykkelse,
                            cap = StrokeCap.Round
                        )

                        // Pilhoder
                        val vinkel = kotlin.math.atan2(sluttY - startY, sluttX - startX)
                        val pilSpiss1X = (sluttX - pilHodeLengde * cos(vinkel - pilHodeVinkel))
                        val pilSpiss1Y = (sluttY - pilHodeLengde * sin(vinkel - pilHodeVinkel))
                        val pilSpiss2X = (sluttX - pilHodeLengde * cos(vinkel + pilHodeVinkel))
                        val pilSpiss2Y = (sluttY - pilHodeLengde * sin(vinkel + pilHodeVinkel))

                        drawLine(
                            color = primaryColor,
                            start = Offset(sluttX, sluttY),
                            end = Offset(pilSpiss1X, pilSpiss1Y),
                            strokeWidth = pilTykkelse,
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = primaryColor,
                            start = Offset(sluttX, sluttY),
                            end = Offset(pilSpiss2X, pilSpiss2Y),
                            strokeWidth = pilTykkelse,
                            cap = StrokeCap.Round
                        )
                    }

                    // Høyre piler (3 piler fra KI-boks til høyre)
                    for (i in -1..1) {
                        val startX = sentrum.x + 70f
                        val startY = sentrum.y + i * 20f
                        val sluttX = hoyreX - 50f
                        val sluttY = sentrum.y + i * vertikalAvstand

                        // Hovedlinjen
                        drawLine(
                            color = primaryColor,
                            start = Offset(startX, startY),
                            end = Offset(sluttX, sluttY),
                            strokeWidth = pilTykkelse,
                            cap = StrokeCap.Round
                        )

                        // Pilhoder
                        val vinkel = kotlin.math.atan2(sluttY - startY, sluttX - startX)
                        val pilSpiss1X = (sluttX - pilHodeLengde * cos(vinkel - pilHodeVinkel))
                        val pilSpiss1Y = (sluttY - pilHodeLengde * sin(vinkel - pilHodeVinkel))
                        val pilSpiss2X = (sluttX - pilHodeLengde * cos(vinkel + pilHodeVinkel))
                        val pilSpiss2Y = (sluttY - pilHodeLengde * sin(vinkel + pilHodeVinkel))

                        drawLine(
                            color = primaryColor,
                            start = Offset(sluttX, sluttY),
                            end = Offset(pilSpiss1X, pilSpiss1Y),
                            strokeWidth = pilTykkelse,
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = primaryColor,
                            start = Offset(sluttX, sluttY),
                            end = Offset(pilSpiss2X, pilSpiss2Y),
                            strokeWidth = pilTykkelse,
                            cap = StrokeCap.Round
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        KortFarge("Vær", MaterialTheme.colorScheme.secondary, tekstFarge = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        KortFarge("Tid", MaterialTheme.colorScheme.secondary, tekstFarge = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        KortFarge("Sted", MaterialTheme.colorScheme.secondary, tekstFarge = MaterialTheme.colorScheme.primary)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.background,
                                        modifier = Modifier.size(40.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "KI",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ML-modell",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // Høyre kolonne: Risikonivåer
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        KortFarge("Høy", Color(0xFFFF6F61), tekstFarge = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        KortFarge("Økt", Color(0xFFFFEB3B), tekstFarge = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        KortFarge("Moderat", Color(0xFF81C784), tekstFarge = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Forklaring av modell
            Card(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vi har utviklet en regresjonsmodell trent på historiske data rundt ulykker, vær og datoer fra perioden 2000-2024." +
                                " Modellen analyserer ukedag, dato, temperatur og nedbørsmengde for å forutsi antall ulykker for et gitt fylke. " +
                                "Den beregner en normalisert verdi som vi oversetter til tre enkle risikonivåer: MODERAT, ØKT" +
                                " og HØY. Vår Kunstige Intelligens, Tryggve, benytter seg deretter av denne modellen, værdata og statistikk om" +
                                " risikomomenter i trafikken, og gir deretter en mer detaljert anbefaling om hva en bør være ekstra obs på, gitt forholdene.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Predikert risiko er ment som en pekepinn, ikke en absolutt verdi.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun KortFarge(
    title: String,
    color: Color,
    iconText: String? = null,
    height: Dp = 60.dp,
    tekstFarge: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(height),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconText != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(32.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = iconText,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = tekstFarge, //Endres ikke av tema
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "Om Tryggve - Lys modus")
@Composable
fun OmTryggvePreviewLight() {
    Team1Theme(darkTheme = false) {
        OmTryggveSkjerm(rememberNavController())
    }
}

@Preview(showBackground = true, name = "Om Tryggve - Mørk modus")
@Composable
fun OmTryggvePreviewDark() {
    Team1Theme(darkTheme = true) {
        OmTryggveSkjerm(rememberNavController())
    }
}