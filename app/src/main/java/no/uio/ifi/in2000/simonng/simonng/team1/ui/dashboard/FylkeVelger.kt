package no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker

@Composable
fun FylkeVelger(
    modifier: Modifier = Modifier,
    naavaerendeFylke: Fylker,
    onFylkeValgt: (Fylker) -> Unit,
    fylkeRisikoNivaa: Map<String, String> = emptyMap(),
    visRisiko: Boolean = false
) {
    var erFylkeVelgerSynlig by remember { mutableStateOf(false) }
    val tilgjengeligeFylker = remember { Fylker.entries }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Lokasjon",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = naavaerendeFylke.visningsnavn,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (visRisiko) { //Beholder denne funksjonaliteten for architecture, dersom en ønsker å videreutvikle appen
                        val fylkeNavn = naavaerendeFylke.visningsnavn
                        val risikoNivaa = fylkeRisikoNivaa[fylkeNavn]
                        if (risikoNivaa != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            RisikoIndikator(risikoNivaa = risikoNivaa)
                        }
                    }
                }

                TextButton(
                    onClick = { erFylkeVelgerSynlig = !erFylkeVelgerSynlig },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = if (erFylkeVelgerSynlig) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (erFylkeVelgerSynlig) "Skjul fylkeliste" else "Vis fylkeliste",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(
                visible = erFylkeVelgerSynlig,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
            ) {
                FylkeListe(
                    tilgjengeligeFylker = tilgjengeligeFylker,
                    onFylkeValgt = { fylke ->
                        onFylkeValgt(fylke)
                        erFylkeVelgerSynlig = false
                    },
                    fylkeRisikoNivaa = fylkeRisikoNivaa,
                    visRisiko = visRisiko
                )
            }
        }
    }
}

@Composable
fun FylkeListe(
    tilgjengeligeFylker: List<Fylker>,
    onFylkeValgt: (Fylker) -> Unit,
    fylkeRisikoNivaa: Map<String, String>,
    visRisiko: Boolean = false
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(tilgjengeligeFylker) { fylke ->
            val fylkeNavn = fylke.visningsnavn

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFylkeValgt(fylke) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fylkeNavn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    if (visRisiko) {
                        val risikoNivaa = fylkeRisikoNivaa[fylkeNavn]
                        if (risikoNivaa != null) {
                            RisikoIndikator(risikoNivaa = risikoNivaa)
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun RisikoIndikator(risikoNivaa: String) {
    val farge = when (risikoNivaa.uppercase()) {
        "MOD" -> Color(0xFF00D000)
        "ØKT" -> Color(0xFFFFC900)
        "HØY" -> Color(0xFFFF0000)
        else -> Color(0xFF888888)
    }

    Surface(
        color = farge,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Text(
            text = risikoNivaa,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}