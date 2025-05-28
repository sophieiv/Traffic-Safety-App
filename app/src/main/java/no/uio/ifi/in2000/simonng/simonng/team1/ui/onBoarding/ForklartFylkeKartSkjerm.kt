package no.uio.ifi.in2000.simonng.simonng.team1.ui.onBoarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.simonng.simonng.team1.R

@Composable
fun ForklartFylkeKartSkjerm() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Telefon mockup med fylkekart-bildet
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Telefon ramme
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(0.5f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Fylkekart-bildet
                    Image(
                        painter = painterResource(id = R.drawable.onboarding_kart),
                        contentDescription = "Fylkekart skjermbilde",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                }
            }
        }

        // Tekst under telefonen
        Text(
            text = "Kart-skjermen",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Benytt deg av knappen øverst for å endre valgt fylke i kartet, eller trykk på fylket du ønsker å velge i kartet.\n\n" +
                        "Fylkene har fått farger utifra hvilken risiko de har:\n" +
                        "- GRØNN = MODERAT RISIKO\n" +
                        "- GUL = ØKT RISIKO\n" +
                        "- RØD = HØY RISIKO",
                textAlign = TextAlign.Left,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.tryggve_raad),
                contentDescription = "Tryggve gir råd",
                modifier = Modifier
                    .size(70.dp)
                    .padding(top = 10.dp)
            )
        }
        }
    }