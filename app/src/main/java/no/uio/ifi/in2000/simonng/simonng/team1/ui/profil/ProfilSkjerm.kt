package no.uio.ifi.in2000.simonng.simonng.team1.ui.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.simonng.simonng.team1.data.navigasjon.Skjerm
import no.uio.ifi.in2000.simonng.simonng.team1.ui.info.BrukerViewModel
import no.uio.ifi.in2000.simonng.simonng.team1.ui.theme.MorkeBlaa
import no.uio.ifi.in2000.simonng.simonng.team1.ui.theme.Team1Theme

@Composable
fun ProfilScreen(
    navController: NavController,
    brukerViewModel: BrukerViewModel = viewModel(),
    onKiModellClick: () -> Unit = { navController.navigate(Skjerm.OmML.rute) },
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {}
) {
    val brukerNavn by brukerViewModel.brukerNavn.observeAsState("")
    var visNavnDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hovedinnhold med scroll som omfatter alt
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Brukernavn-kort
            Kort(
                height = 74.dp,
                backgroundColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable { visNavnDialog = true }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = brukerNavn.ifEmpty { "Ikke angitt" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Trykk for å endre",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mørk Modus-kort
            Kort(
                height = 74.dp,
                backgroundColor = MaterialTheme.colorScheme.secondary
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mørk Modus",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                            uncheckedBorderColor = MaterialTheme.colorScheme.primary,
                            checkedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = true).heightIn(min = 32.dp))

            Text(
                text = "Om Tryggve",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
                    .clickable { onKiModellClick() },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "KI",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.small)
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.width(30.dp))
                    Text(
                        text = "Lær mer om hvordan vi beregner risiko",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Navnendring-dialog
    if (visNavnDialog) {
        NavnEndringsDialog(
            gjeldendeBrukerNavn = brukerNavn,
            onAvvis = { visNavnDialog = false },
            onLagre = { nyttNavn ->
                brukerViewModel.lagreBrukerNavn(nyttNavn)
                visNavnDialog = false
            }
        )
    }
}

@Composable
fun NavnEndringsDialog(
    gjeldendeBrukerNavn: String,
    onAvvis: () -> Unit,
    onLagre: (String) -> Unit
) {
    var navnInndata by remember { mutableStateOf(gjeldendeBrukerNavn) }

    AlertDialog(
        onDismissRequest = onAvvis,
        containerColor = MaterialTheme.colorScheme.secondary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        title = { Text("Endre navn") },
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
                        focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = MaterialTheme.colorScheme.secondary,
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
                colors = ButtonDefaults.buttonColors(containerColor = MorkeBlaa)
            ) { Text("Lagre", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onAvvis) { Text("Avbryt", color = MaterialTheme.colorScheme.primary) }
        }
    )
}

@Composable
fun Kort(
    modifier: Modifier = Modifier,
    height: Dp,
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        content()
    }
}

@Preview(showBackground = true, name = "Lys modus")
@Composable
fun ProfilScreenPreviewLight() {
    Team1Theme(darkTheme = false) {
        ProfilScreen(
            navController = rememberNavController(),
            isDarkMode = false,
            onToggleDarkMode = {},
            onKiModellClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Mørk modus")
@Composable
fun ProfilScreenPreviewDark() {
    Team1Theme(darkTheme = true) {
        ProfilScreen(
            navController = rememberNavController(),
            isDarkMode = true,
            onToggleDarkMode = {},
            onKiModellClick = {}
        )
    }
}