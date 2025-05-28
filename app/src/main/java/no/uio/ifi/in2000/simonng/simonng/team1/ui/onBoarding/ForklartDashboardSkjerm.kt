package no.uio.ifi.in2000.simonng.simonng.team1.ui.onBoarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
fun ForklartDashboardSkjerm() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Telefon mockup med dashboard-bildet
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
                    // Dashboard-bildet
                    Image(
                        painter = painterResource(id = R.drawable.onboarding_dashboard),
                        contentDescription = "Dashboard skjermbilde",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                }
            }
        }
        // Tekst under telefonen
        Text(
            text = "Hjem-skjermen",
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
            text = "Her kan du velge din posisjon øverst.\n\n" +
                    "Jeg vil deretter gi deg en anbefaling om hva du bør ta ekstra hensyn til gitt dagens forhold og risikonivå.\n\n" +
                    "For å se værmelding og risiko for de kommende dagene, kan du trykke på '10 dager' helt nederst på siden.",
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