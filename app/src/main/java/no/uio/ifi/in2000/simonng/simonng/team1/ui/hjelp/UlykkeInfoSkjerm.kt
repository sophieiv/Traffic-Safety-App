package no.uio.ifi.in2000.simonng.simonng.team1.ui.hjelp

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UlykkeInfoSkjerm(navController: NavHostController) {
    val scrollState = rememberScrollState()
    val utvidet = remember { mutableStateListOf(false, false, false, false, false) }

    val titler = listOf(
        "1. Sikre stedet",
        "2. Få oversikt over situasjonen",
        "3. Varsle nødetater",
        "4. Gi livreddende førstehjelp",
        "5. Hjelp til med mindre skader"
    )

    val beskrivelser = listOf(
        "• Sørg for at stedet er trygt før du hjelper andre.\n\n" +
                "• Sett på nødblink, ta på refleksvest, og plasser varseltrekant ca. 150 meter fra ulykkesstedet (motorvei: 250 meter).\n\n" +
                "• Ikke utsett deg selv eller andre for fare.\n\n" +
                "• Vurder trafikken, brannfare eller farlige væsker.",

        "• Sjekk hvor mange som er skadet og hva som har skjedd.\n\n" +
                "• Ta en rask vurdering: Er det bevisstløse personer?\n\n" +
                "• Blør noen kraftig?\n\n" +
                "• Har noen pustevansker?\n\n" +
                "Dette hjelper deg med å gi riktige opplysninger når du ringer 113.",

        "• Ring 113 ved alvorlig skade eller fare for liv.\n\n" +
                "• Gi tydelig beskjed om hvor du er, hva som har skjedd, og hvor mange som er involvert.\n\n" +
                "• Følg instruksene fra operatøren.\n\n" +
                "• 113 kan veilede deg i førstehjelp over telefon.",

        "• For bevisstløs person med unormal/ingen pust, utfør hjerte-lungeredning (HLR).\n\n" +
                "• (HLR): Start med 30 brystkompresjoner + 2 innblåsninger.\n\n" +
                "• Dersom du ikke kan gi innblåsninger, gjør bare kompresjoner.\n\n" +
                "• Fortsett til hjelp kommer.\n\n" +
                "• Bruk hjertestarter (AED) hvis tilgjengelig – den forklarer hva du skal gjøre.",

        "• Stans blødning og hold den skadde varm.\n\n" +
                "• Trykk på blødninger med noe rent.\n\n" +
                "• Ikke flytt skadde unødvendig.\n\n" +
                "• Dekk til med jakke eller teppe for å holde på kroppsvarmen.\n\n" +
                "• Snakk rolig til personen."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Instrukser ved ulykke") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbake"
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
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Overordnede instrukser kort
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Overordnede instrukser",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Stans, sett på nødblink og ta på deg refleksvest.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Få oversikt og deretter sikre skadestedet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Ved personskade – gi nødvendig førstehjelp.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Utvidbare instruksjonskort
            titler.forEachIndexed { indeks, tittel ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (utvidet[indeks])
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { utvidet[indeks] = !utvidet[indeks] }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tittel,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = if (utvidet[indeks]) "Lukk" else "Åpne",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.rotate(if (utvidet[indeks]) 180f else 0f)
                            )
                        }
                        if (utvidet[indeks]) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = beskrivelser[indeks],
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}