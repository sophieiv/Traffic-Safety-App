package no.uio.ifi.in2000.simonng.simonng.team1.ui.onBoarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.simonng.simonng.team1.R

@Composable
fun VelkommenSkjerm() {
    Scaffold { paddingValue ->
        Box(
            modifier = Modifier
                .padding(paddingValue)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // Tryggve-bilde
                Image(
                    painter = painterResource(id = R.drawable.tryggve_vinker),
                    contentDescription = "Tryggve vinker",
                    modifier = Modifier
                        .size(220.dp)
                        .padding(top = 24.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Velkommen! Mitt navn er Tryggve og jeg ønsker gjerne å gi deg en guide på hvordan du kan bruke appen! Er du med?",
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}