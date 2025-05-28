package no.uio.ifi.in2000.simonng.simonng.team1.ui.dashboard

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class PromptBuilder {

    fun byggTipsPrompt(temperatur: Float, nedbor: Float): String {
        val vaerBeskrivelse = generervaerBeskrivelse(nedbor)
        val temperaturBeskrivelse = genererTemperaturBeskrivelse(temperatur)
        val tidPaDognet = genererTidsbeskrivelse(LocalTime.now().hour)
        val aarstidBeskrivelse = genererAarstidsbeskrivelse(LocalDate.now().monthValue)
        val trafikkmengdeBeskrivelse = genererTrafikkmengdeBeskrivelse(LocalTime.now().hour, LocalDate.now().dayOfWeek.value)
        val ukedag = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE", Locale("no", "NO")))
        val maaned = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM", Locale("no", "NO")))

        //Merk "ikke nevn snø, is, frost, kaldt være, glatte veier", ettersom botten har en tendens til å nevne disse adjektivene/subjektivene ved sommerforhold
        return """
        Du er Tryggve, en vennlig og erfaren trafikkrådgiver som gir korte, presise råd til bilførere.
        
        Gi TO konkrete trafikkråd (start med "På grunn av...") til meg, kondensert til én setninger, basert på:
        • $ukedag i $maaned med $aarstidBeskrivelse
        • $vaerBeskrivelse med $temperaturBeskrivelse
        • $tidPaDognet med $trafikkmengdeBeskrivelse
        
        Denne setningen skal være på maks 30 ord. 
        
        Ikke nevne snø, is, frost, kaldt vær, glatte veier, vinter eller lignende forhold – uansett. Ikke nevn noe med uventede manøvrer. Vær kreativ.
        Svar maks 3 setninger. Ikke gi generelle råd – bruk kun faktorene over. Svar som om du snakker direkte til meg. Det skal være relvant for en bruker.
        IKKE skriv at man skal være ekstra oppmerksom for lengre bremselengde grunnet tørt føre. Dersom det er vår/sommer og $tidPaDognet er før 22.00, ikke si
        at det er dårlig sikt, med mindre $vaerBeskrivelse inneholder nedbør/tåke.
        """.trimIndent()

    }

    fun byggRisikoForklaringPrompt(fylke: String, risiko: String, temperatur: Float, nedbor: Float): String {
        val vaerBeskrivelse = generervaerBeskrivelse(nedbor)
        val temperaturBeskrivelse = genererTemperaturBeskrivelse(temperatur)
        val tidPaDognet = genererTidsbeskrivelse(LocalTime.now().hour)

        val trafikkmengdeBeskrivelse = genererTrafikkmengdeBeskrivelse(LocalTime.now().hour, LocalDate.now().dayOfWeek.value)
        val ukedag = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE", Locale("no", "NO")))

        return """
        Du er Tryggve, en vennlig trafikkrådgiver med dyp forståelse av sikkerhet på veien.
        
        Gi en kort forklaring (maks 20 ord) på hvorfor det er $risiko risiko i $fylke akkurat nå. Ikke bruk tørt vær og glatte veier
        i samme forklaring. Disse henger aldri sammen. Dersom det er vår/sommer og $tidPaDognet er før 22.00, ikke si
        at det er dårlig sikt, med mindre $vaerBeskrivelse inneholder nedbør/tåke.
        
        Dagens forhold:
        • $ukedag 
        • $vaerBeskrivelse, $temperaturBeskrivelse
        • $tidPaDognet med $trafikkmengdeBeskrivelse
        
        Fokuser bare på de mest relevante faktorene ovenfor som faktisk påvirker risikoen i dag.
        """.trimIndent()
    }

    private fun generervaerBeskrivelse(nedbor: Float) = when {
        nedbor <= 0f -> "tørt oppholdsvær"
        nedbor < 0.5f -> "lett yr"
        nedbor < 2f -> "lett regn"
        nedbor < 10f -> "moderat regn"
        else -> "kraftig regn"
    }

    private fun genererTemperaturBeskrivelse(temperatur: Float) = when {
        temperatur < -10 -> "ekstremt kaldt (${temperatur.toInt()}°C)"
        temperatur < -5 -> "veldig kaldt (${temperatur.toInt()}°C)"
        temperatur < 0 -> "kuldegrader (${temperatur.toInt()}°C)"
        temperatur < 5 -> "kjølig (${temperatur.toInt()}°C)"
        temperatur < 16 -> "${temperatur.toInt()}°C"
        temperatur < 25 -> "varmt (${temperatur.toInt()}°C)"
        else -> "svært varmt (${temperatur.toInt()}°C)"
    }

    private fun genererTidsbeskrivelse(time: Int) = when(time) {
        in 5..8 -> "morgentime (rush)"
        in 9..14 -> "dagtid"
        in 15..17 -> "ettermiddagsrush"
        in 18..21 -> "kveld"
        else -> "natt"
    }

    private fun genererAarstidsbeskrivelse(maaned: Int) = when(maaned) {
        in 3..5 -> "vår"
        in 6..8 -> "sommer"
        in 9..11 -> "høst"
        else -> "vinter"
    }

    private fun genererTrafikkmengdeBeskrivelse(time: Int, ukedag: Int): String {
        val erHelg = ukedag > 5  // 6 = lørdag, 7 = søndag

        return when {
            erHelg && time in 10..18 -> "moderat helgetrafikk"
            erHelg -> "lett helgetrafikk"
            time in 7..9 -> "høy (morgenrush)"
            time in 15..17 -> "høy (ettermiddagsrush)"
            time in 11..14 -> "moderat"
            else -> "lett"
        }
    }
}