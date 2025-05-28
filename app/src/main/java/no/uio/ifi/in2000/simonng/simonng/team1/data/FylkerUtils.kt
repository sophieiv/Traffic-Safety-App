package no.uio.ifi.in2000.simonng.simonng.team1.data

object FylkeUtils {
    // Risiko-nivå konstanter
    const val RISIKO_UKJENT = "Ukjent"
    const val RISIKO_LASTER = "Laster..."
    const val RISIKO_MODERAT = "MOD"
    const val RISIKO_OEKT = "ØKT"
    const val RISIKO_HOEY = "HØY"

    // Risiko-nivå terskelverdier (satt på bakgrunn av testing med ML-modellen)
    private const val ML_THRESHOLD_MOD = 0.45f
    private const val ML_THRESHOLD_OKT = 0.51f

    // Risiko-farger
    val RISIKO_FARGER = mapOf(
        RISIKO_MODERAT to "#EEB91C",
        RISIKO_OEKT to "#FF5722",
        RISIKO_HOEY to "#730D06",
        RISIKO_LASTER to "#888888",
        RISIKO_UKJENT to "#BBBBBB",
        "normal_fylke" to "#888888"
    )

    // UI-farger for risikoindikator
    val RISIKO_UI_FARGER = mapOf(
        RISIKO_MODERAT to Pair(0xFF00D000, 0xFF000000), // Grønn, svart tekst
        RISIKO_OEKT to Pair(0xFFFFC900, 0xFF000000),    // Gul, svart tekst
        RISIKO_HOEY to Pair(0xFFFF0000, 0xFFFFFFFF),    // Rød, hvit tekst
        RISIKO_LASTER to Pair(0x80808080, 0xFFFFFFFF),  // Grå med 50% alpha, hvit tekst
        RISIKO_UKJENT to Pair(0xFFD3D3D3, 0xFF000000)   // Lysegrå, svart tekst
    )

    // Hjelpemetode for å bestemme risikonivå basert på verdi
    fun bestemRisikoNivaa(antall: Float?): String {
        val resultat = when {
            antall == null -> RISIKO_UKJENT
            antall < 0 -> RISIKO_UKJENT
            antall < ML_THRESHOLD_MOD -> RISIKO_MODERAT
            antall < ML_THRESHOLD_OKT -> RISIKO_OEKT
            else -> RISIKO_HOEY
        }
        return resultat
    }

    fun hentRisikoFarge(
        fylkeNavn: String,
        fylkeRisikoNivaa: Map<String, String>,
        fylkeFarger: Map<String, String>
    ): String {
        val risikoNivaa = fylkeRisikoNivaa[fylkeNavn] ?: RISIKO_LASTER

        return when (risikoNivaa) {
            RISIKO_MODERAT -> fylkeFarger[RISIKO_MODERAT] ?: RISIKO_FARGER[RISIKO_MODERAT]!!
            RISIKO_OEKT -> fylkeFarger[RISIKO_OEKT] ?: RISIKO_FARGER[RISIKO_OEKT]!!
            RISIKO_HOEY -> fylkeFarger[RISIKO_HOEY] ?: RISIKO_FARGER[RISIKO_HOEY]!!
            RISIKO_UKJENT -> fylkeFarger[RISIKO_UKJENT] ?: RISIKO_FARGER[RISIKO_UKJENT]!!
            RISIKO_LASTER -> fylkeFarger[RISIKO_LASTER] ?: RISIKO_FARGER[RISIKO_LASTER]!!
            else -> fylkeFarger["normal_fylke"] ?: RISIKO_FARGER["normal_fylke"]!!
        }
    }
}