package no.uio.ifi.in2000.simonng.simonng.team1.data.navigasjon


sealed class Skjerm(val rute: String) {
    data object Onboarding : Skjerm("onboarding")
    data object Dashboard : Skjerm("dashboard")
    data object Profil : Skjerm("profil")
    data object Hjelp : Skjerm("hjelp")
    data object Kart : Skjerm("kart")
    data object OmML : Skjerm("om_ml")
    data object FremtidigVaermelding : Skjerm("10dager_vaermelding")
    data object UlykkeInfo : Skjerm("ulykke_info")
    data object VeihjelpNumre : Skjerm("veihjelp_numre")
}

data class BunnMenyElement(
    val rute: String,
    val aktivIkon: Int,
    val inaktivIkon: Int,
    val navn: String
)