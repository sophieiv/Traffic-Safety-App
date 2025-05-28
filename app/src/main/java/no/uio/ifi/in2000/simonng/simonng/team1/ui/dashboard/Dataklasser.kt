package no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard

import no.uio.ifi.in2000.simonng.simonng.team1.R
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Dataklasse for utvidet varsel
data class DagligVarselData(
    val dato: String,
    val minTemp: Int,
    val maxTemp: Int,
    val ikonResource: Int,
    val risikoNivaa: String
)

data class DashboardUiState(
    val selectedFylke: Fylker,
    val datoInput: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val calculatedRisk: String? = null,
    val currentTipsFylke: Fylker? = null,
    val aiTips: String? = null,
    val temperaturTekst: String = "N/A",
    val utvidetVarsel: List<DagligVarselData> = emptyList(),
    // Felter for risikoforklaring
    val riskExplanation: String? = null,
    val isRiskExplanationLoading: Boolean = false,
    val riskExplanationError: String? = null,
    // Felter for å holde på kontekst for prompt
    val fetchedTempForPrompt: Float? = null, // Faktisk temperatur
    val fetchedNedborForPrompt: Float? = null, // Faktisk nedbør
    val fetchedSymbolCode: String? = null // Værsymbol-kode
)

fun getWeatherIconResId(symbolCode: String?): Int {
    return when (symbolCode?.lowercase()) {
        "clearsky_day" -> R.drawable.clearsky_day
        "clearsky_night" -> R.drawable.clearsky_night
        "clearsky_polartwilight" -> R.drawable.clearsky_polartwilight
        "fair_day" -> R.drawable.fair_day
        "fair_night" -> R.drawable.fair_night
        "fair_polartwilight" -> R.drawable.fair_polartwilight
        "partlycloudy_day" -> R.drawable.partlycloudy_day
        "partlycloudy_night" -> R.drawable.partlycloudy_night
        "partlycloudy_polartwilight" -> R.drawable.partlycloudy_polartwilight
        "cloudy" -> R.drawable.cloudy
        "rainshowers_day" -> R.drawable.rainshowers_day
        "rainshowers_night" -> R.drawable.rainshowers_night
        "rainshowers_polartwilight" -> R.drawable.rainshowers_polartwilight
        "snowshowers_day" -> R.drawable.snowshowers_day
        "snowshowers_night" -> R.drawable.snowshowers_night
        "snowshowers_polartwilight" -> R.drawable.snowshowers_polartwilight
        "rain" -> R.drawable.rain
        "heavyrain" -> R.drawable.heavyrain
        "heavyrainshowers_day" -> R.drawable.heavyrainshowers_day
        "heavyrainshowers_night" -> R.drawable.heavyrainshowers_night
        "heavyrainshowers_polartwilight" -> R.drawable.heavyrainshowers_polartwilight
        "sleet" -> R.drawable.sleet
        "snow" -> R.drawable.snow
        "heavysnow" -> R.drawable.heavysnow
        "fog" -> R.drawable.fog
        else -> R.drawable.partlycloudy_day
    }
}

