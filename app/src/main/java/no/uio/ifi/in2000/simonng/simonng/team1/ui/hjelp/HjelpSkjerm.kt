package no.uio.ifi.in2000.simonng.simonng.team1.ui.hjelp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.simonng.simonng.team1.data.navigasjon.Skjerm

@Composable
fun HjelpSkjerm(
    viewModel: HjelpViewModel = viewModel(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun ringTelefon(element: NodElement) {
        val nummer = viewModel.hentTelefonnummer(element)
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$nummer")
        }
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Nødhjelp",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Nødetater",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Beholder de spesifikke fargene for nødetatene uavhengig av tema
        NodKort(
            ikon = Icons.Default.Call,
            ikonBakgrunn = Color(0xFFD32F2F), // Ambulanse - rød
            tekst = "Ambulanse",
            onClick = { ringTelefon(viewModel.noedetater[0]) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        NodKort(
            ikon = Icons.Default.Call,
            ikonBakgrunn = Color(0xFF0D47A1), // Politi - blå
            tekst = "Politi",
            onClick = { ringTelefon(viewModel.noedetater[1]) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        NodKort(
            ikon = Icons.Default.Call,
            ikonBakgrunn = Color(0xFFFF6F00), // Brannvesen - oransje
            tekst = "Brannvesen",
            onClick = { ringTelefon(viewModel.noedetater[2]) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Ulykke",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        NodKort(
            ikon = Icons.Default.Warning,
            ikonBakgrunn = MaterialTheme.colorScheme.primary,
            tekst = "Instrukser ved ulykke",
            onClick = { navController.navigate(Skjerm.UlykkeInfo.rute) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        NodKort(
            ikon = Icons.Default.DirectionsCar,
            ikonBakgrunn = MaterialTheme.colorScheme.primary,
            tekst = "Veihjelpsnumre",
            onClick = { navController.navigate(Skjerm.VeihjelpNumre.rute) }
        )
    }
}

@Composable
fun NodKort(
    ikon: ImageVector,
    ikonBakgrunn: Color,
    tekst: String,
    onClick: () -> Unit
) {
    val ikonFarge = if (
        tekst == "Ambulanse" ||
        tekst == "Politi" ||
        tekst == "Brannvesen"
    ) {
        Color.White
    } else {
        val isDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5
        if (isDarkMode) Color.DarkGray else Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ikonBakgrunn, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ikon,
                    contentDescription = null,
                    tint = ikonFarge,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = tekst,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}