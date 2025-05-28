package no.uio.ifi.in2000.simonng.simonng.team1.ui.fylkekart

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar
import no.uio.ifi.in2000.simonng.simonng.team1.data.FylkeUtils
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import no.uio.ifi.in2000.simonng.simonng.team1.ui.NettverkBanner

@Composable
fun FylkeKartSkjerm(
    modifier: Modifier = Modifier,
    fylkeKartViewModel: FylkeKartViewModel
) {
    val kontekst = LocalContext.current

    // Laster fylkedata ved oppstart
    LaunchedEffect(Unit) {
        fylkeKartViewModel.lastFylkeData(kontekst)
    }

    // Samler inn state fra ViewModeller
    val naavaerendeFylke by fylkeKartViewModel.naavaerendeFylke.collectAsState()
    val fylkeData by fylkeKartViewModel.fylkeData.collectAsState()
    val lasterData by fylkeKartViewModel.lasterData.collectAsState()
    val kameraInnstillinger by fylkeKartViewModel.kameraInnstillinger.collectAsState()
    val fylkeFarger by fylkeKartViewModel.fylkeFarger.collectAsState()
    val fylkeLagInnstillinger by fylkeKartViewModel.fylkeLagInnstillinger.collectAsState()
    val fylkeRisikoNivaa by fylkeKartViewModel.fylkeRisikoNivaa.collectAsState()
    val kartStil by fylkeKartViewModel.kartStil.collectAsState()


    // Mapbox
    Box(modifier = modifier.fillMaxSize()) {
        if (fylkeData != null && !lasterData) {
            MapBoxVisning(
                naavaerendeFylkeId = naavaerendeFylke.name.lowercase(),
                fylkeData = fylkeData,
                kameraInnstillinger = kameraInnstillinger,
                fylkeFarger = fylkeFarger,
                fylkeLagInnstillinger = fylkeLagInnstillinger,
                kartStil = kartStil,
                fylkeRisikoNivaa = fylkeRisikoNivaa,
                onFylkeKlikket = { fylke -> fylkeKartViewModel.oppdaterValgtFylke(fylke) }
            )
        } else {
            // Laster
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        // Fylkevelger widget
        FylkeVelger(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .align(Alignment.TopCenter),
            naavaerendeFylke = naavaerendeFylke,
            onFylkeValgt = { fylke -> fylkeKartViewModel.oppdaterValgtFylke(fylke) },
            fylkeRisikoNivaa = fylkeRisikoNivaa
        )
        NettverkBanner(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            skjerm = "fylkekart"
        )
    }
}

@Composable
private fun MapBoxVisning(
    naavaerendeFylkeId: String,
    fylkeData: FeatureCollection?,
    kameraInnstillinger: com.mapbox.maps.CameraOptions,
    fylkeFarger: Map<String, String>,
    fylkeLagInnstillinger: Map<String, Any>,
    kartStil: String = Style.MAPBOX_STREETS,
    fylkeRisikoNivaa: Map<String, String> = emptyMap(),
    onFylkeKlikket: (Fylker) -> Unit = {}
) {
    val kontekst = LocalContext.current
    val mapView = remember { MapView(kontekst) }
    var erStilLastet by remember { mutableStateOf(false) }
    var sisteLastedeFylkeData by remember { mutableStateOf<FeatureCollection?>(null) }
    var sisteValgtFylkeId by remember { mutableStateOf("") } // Spor sist valgte fylke


    // Sjekker om fylket har endret seg siden forrige render
    val harValgtFylkeEndretSeg = sisteValgtFylkeId != naavaerendeFylkeId

    AndroidView(
        factory = { _ -> mapView },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            // Laster stil og data hvis det er første gang, eller dersom data har endret seg
            if (!erStilLastet || fylkeData != sisteLastedeFylkeData) {
                view.mapboxMap.loadStyle(kartStil) { stil ->
                    erStilLastet = true
                    sisteLastedeFylkeData = fylkeData
                    sisteValgtFylkeId = naavaerendeFylkeId

                    // Konfigurerer kartet
                    konfigurerKart(
                        stil = stil,
                        view = view,
                        fylkeData = fylkeData,
                        naavaerendeFylkeId = naavaerendeFylkeId,
                        fylkeFarger = fylkeFarger,
                        fylkeLagInnstillinger = fylkeLagInnstillinger,
                        fylkeRisikoNivaa = fylkeRisikoNivaa,
                        onFylkeKlikket = onFylkeKlikket
                    )
                }
            } else {
                view.mapboxMap.getStyle { stil ->
                    oppdaterFylkeFarger(stil, fylkeRisikoNivaa, fylkeFarger)

                    if (harValgtFylkeEndretSeg) {
                        oppdaterFylkeKontur(stil, naavaerendeFylkeId)
                        sisteValgtFylkeId = naavaerendeFylkeId
                    }

                    view.mapboxMap.setCamera(kameraInnstillinger)
                }
            }
        }
    )
}

private fun oppdaterFylkeKontur(
    stil: Style,
    naavaerendeFylkeId: String
) {
    try {
        stil.getLayer("fylker-kontur")?.let { lag ->
            (lag as? LineLayer)?.filter(
                Expression.not(byggValgtFylkeFilter(naavaerendeFylkeId))
            )
        }

        // Oppdaterer kontur for valgt fylke
        stil.getLayer("valgt-fylke-kontur")?.let { lag ->
            (lag as? LineLayer)?.filter(
                byggValgtFylkeFilter(naavaerendeFylkeId)
            )
        }
    } catch (e: Exception) {
        Log.e("MapBoxVisning", "Feil ved oppdatering av fylkekontur: ${e.message}")
    }
}

private fun konfigurerKart(
    stil: Style,
    view: MapView,
    fylkeData: FeatureCollection?,
    naavaerendeFylkeId: String,
    fylkeFarger: Map<String, String>,
    fylkeLagInnstillinger: Map<String, Any>,
    fylkeRisikoNivaa: Map<String, String>,
    onFylkeKlikket: (Fylker) -> Unit
) {
    // Grunnleggende kartoppsett
    view.scalebar.enabled = false
    view.compass.enabled = false

    // Konfigurerer gestures for kartnavigasjon
    view.gestures.apply {
        pinchToZoomEnabled = true
        scrollEnabled = true
        doubleTapToZoomInEnabled = true
        quickZoomEnabled = true
        doubleTouchToZoomOutEnabled = true
        scrollDecelerationEnabled = true
        pinchToZoomDecelerationEnabled = true
        rotateDecelerationEnabled = true
    }

    // Fjerner gamle lag
    try {
        stil.removeStyleLayer("valgt-fylke-kontur")
        stil.removeStyleLayer("fylker-kontur")
        stil.removeStyleLayer("fylker-fyll")
        stil.removeStyleSource("fylker-kilde")
    } catch (e: Exception) {
        // Ignorerer feil ettersom vi ikke ønsker å gjøre noe mer håndtering
    }

    // Legger til nye kilder og lag
    fylkeData?.let { data ->
        // Legger til GeoJSON-kilde
        stil.addSource(
            GeoJsonSource.Builder("fylker-kilde")
                .data(data.toJson())
                .build()
        )

        stil.addLayer(FillLayer("fylker-fyll", "fylker-kilde").apply {
            fillOpacity(fylkeLagInnstillinger["fyll_opacity"] as Double)
            fillColor(byggFylkeFargeUttrykk(fylkeRisikoNivaa, fylkeFarger))
        })

        stil.addLayer(LineLayer("fylker-kontur", "fylker-kilde").apply {
            lineWidth(fylkeLagInnstillinger["kontur_bredde"] as Double)
            lineColor(fylkeFarger["kontur"]!!)
            filter(Expression.not(byggValgtFylkeFilter(naavaerendeFylkeId)))
        })

        // Valgt fylke
        stil.addLayer(LineLayer("valgt-fylke-kontur", "fylker-kilde").apply {
            lineWidth((fylkeLagInnstillinger["kontur_bredde"] as Double) * 2.5)
            lineColor(fylkeFarger["valgt_fylke"]!!)
            filter(byggValgtFylkeFilter(naavaerendeFylkeId))
        })
    }

    // Setter opp klikkhåndtering
    view.gestures.addOnMapClickListener { point ->
        val lng = point.longitude()
        val lat = point.latitude()

        val fylke = when {
            lng in 10.5..11.0 && lat in 59.8..60.0 -> Fylker.OSLO
            lng in 10.5..11.5 && lat in 59.6..60.2 -> Fylker.AKERSHUS
            lng in 9.0..10.5 && lat in 59.5..60.5 -> Fylker.BUSKERUD
            lng in 9.9..10.4 && lat in 59.0..59.7 -> Fylker.VESTFOLD
            lng in 10.8..11.7 && lat in 58.8..59.7 -> Fylker.OESTFOLD
            else -> null
        }
        fylke?.let { onFylkeKlikket(it) }
        true
    }
}

private fun oppdaterFylkeFarger(
    stil: Style,
    fylkeRisikoNivaa: Map<String, String>,
    fylkeFarger: Map<String, String>
) {
    try {
        stil.getLayer("fylker-fyll")?.let { lag ->
            (lag as? FillLayer)?.fillColor(
                byggFylkeFargeUttrykk(fylkeRisikoNivaa, fylkeFarger)
            )
        }
    } catch (e: Exception) {
        // Ignorerer ettersom vi ikke ønsker å gjøre noe mer håndtering
    }
}

private fun byggFylkeFargeUttrykk(
    fylkeRisikoNivaa: Map<String, String>,
    fylkeFarger: Map<String, String>
): Expression {
    return Expression.match(
        Expression.get(Expression.literal("id")),

        Expression.literal(Fylker.OSLO.fylkesId),
        Expression.literal(FylkeUtils.hentRisikoFarge(Fylker.OSLO.visningsnavn, fylkeRisikoNivaa, fylkeFarger)),

        Expression.literal(Fylker.AKERSHUS.fylkesId),
        Expression.literal(FylkeUtils.hentRisikoFarge(Fylker.AKERSHUS.visningsnavn, fylkeRisikoNivaa, fylkeFarger)),

        Expression.literal(Fylker.BUSKERUD.fylkesId),
        Expression.literal(FylkeUtils.hentRisikoFarge(Fylker.BUSKERUD.visningsnavn, fylkeRisikoNivaa, fylkeFarger)),

        Expression.literal(Fylker.VESTFOLD.fylkesId),
        Expression.literal(FylkeUtils.hentRisikoFarge(Fylker.VESTFOLD.visningsnavn, fylkeRisikoNivaa, fylkeFarger)),

        Expression.literal(Fylker.OESTFOLD.fylkesId),
        Expression.literal(FylkeUtils.hentRisikoFarge(Fylker.OESTFOLD.visningsnavn, fylkeRisikoNivaa, fylkeFarger)),

        // Fallback for normal fylke
        Expression.literal(fylkeFarger["normal_fylke"] ?: FylkeUtils.RISIKO_FARGER["normal_fylke"]!!)
    )
}


private fun byggValgtFylkeFilter(naavaerendeFylkeId: String): Expression {
    // Konverterer fylke-ID til faktisk fylke
    val fylke = Fylker.entries.find { it.name.lowercase() == naavaerendeFylkeId.lowercase() }

    val fylkeId = fylke?.fylkesId ?: ""

    if (fylkeId.isNotEmpty()) {
        Log.d("MapBoxVisning", "Bygger filter for fylke: ${fylke?.visningsnavn}, ID: $fylkeId")
        return Expression.eq(Expression.literal("id"), Expression.literal(fylkeId))
    } else {
        // Fallback hvis fylket ikke ble funnet
        Log.e("MapBoxVisning", "Kunne ikke finne fylket for ID: $naavaerendeFylkeId")
        return Expression.eq(Expression.literal("id"), Expression.literal(""))
    }
}

@Composable
private fun FylkeVelger(
    modifier: Modifier = Modifier,
    naavaerendeFylke: Fylker,
    onFylkeValgt: (Fylker) -> Unit,
    fylkeRisikoNivaa: Map<String, String> = emptyMap()
) {
    var erFylkeVelgerSynlig by remember { mutableStateOf(false) }
    val tilgjengeligeFylker = remember { Fylker.TILGJENGELIGE_FYLKER }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                    // Viser risikonivå for valgt fylke
                    val fylkeNavn = naavaerendeFylke.visningsnavn
                    val risikoNivaa = fylkeRisikoNivaa[fylkeNavn]
                    if (risikoNivaa != null) {
                        RisikoIndikatorBoks(risikoNivaa = risikoNivaa, tekstFarge = Color.Black)
                    }
                }

                // Vis/skjul knapp for fylkeliste
                TextButton(
                    onClick = { erFylkeVelgerSynlig = !erFylkeVelgerSynlig },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = if (erFylkeVelgerSynlig) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (erFylkeVelgerSynlig) "Skjul fylkevelger" else "Vis fylkevelger",
                        modifier = Modifier.size(28.dp),
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
                    fylkeRisikoNivaa = fylkeRisikoNivaa
                )
            }
        }
    }
}

@Composable
private fun FylkeListe(
    tilgjengeligeFylker: List<Fylker>,
    onFylkeValgt: (Fylker) -> Unit,
    modifier: Modifier = Modifier,
    fylkeRisikoNivaa: Map<String, String> = emptyMap()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Velg fylke",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(tilgjengeligeFylker.size) { index ->
                val fylke = tilgjengeligeFylker[index]
                val fylkeNavn = fylke.visningsnavn
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFylkeValgt(fylke) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fylkeNavn,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val risikoNivaa = fylkeRisikoNivaa[fylkeNavn]
                    if (risikoNivaa != null) {
                        RisikoIndikatorBoks(risikoNivaa = risikoNivaa, tekstFarge = Color.Black)
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun RisikoIndikatorBoks(
    risikoNivaa: String,
    tekstFarge: Color
) {
    val bakgrunnsFarge = when (risikoNivaa) {
        FylkeUtils.RISIKO_MODERAT -> {
            val farger = FylkeUtils.RISIKO_UI_FARGER[FylkeUtils.RISIKO_MODERAT]!!
            Color(farger.first)
        }
        FylkeUtils.RISIKO_OEKT -> {
            val farger = FylkeUtils.RISIKO_UI_FARGER[FylkeUtils.RISIKO_OEKT]!!
            Color(farger.first)
        }
        FylkeUtils.RISIKO_HOEY -> {
            val farger = FylkeUtils.RISIKO_UI_FARGER[FylkeUtils.RISIKO_HOEY]!!
            Color(farger.first)
        }
        FylkeUtils.RISIKO_LASTER -> {
            val farger = FylkeUtils.RISIKO_UI_FARGER[FylkeUtils.RISIKO_LASTER]!!
            Color(farger.first)
        }
        else -> {
            val farger = FylkeUtils.RISIKO_UI_FARGER[FylkeUtils.RISIKO_UKJENT]!!
            Color(farger.first)
        }
    }

    Surface(
        color = bakgrunnsFarge,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        if (risikoNivaa == FylkeUtils.RISIKO_LASTER) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = risikoNivaa,
                    style = MaterialTheme.typography.bodySmall,
                    color = tekstFarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    color = tekstFarge,
                    strokeWidth = 2.dp
                )
            }
        } else {
            Text(
                text = risikoNivaa,
                style = MaterialTheme.typography.bodySmall,
                color = tekstFarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}