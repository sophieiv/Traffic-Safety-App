package no.uio.ifi.in2000.simonng.simonng.team1.ui.hjelp

import androidx.lifecycle.ViewModel

data class NodElement(
    val etikett: String,
    val telefonnummer: String
)

class HjelpViewModel : ViewModel() {


    val noedetater = listOf(
        NodElement("Ambulanse", "113"),
        NodElement("Politi", "112"),
        NodElement("Brannvesen", "110")
    )

    val veihjelp = listOf(
        NodElement("NAF", "08505"),
        NodElement("Falcken", "02222"),
        NodElement("Viking", "06000")
    )

    fun hentTelefonnummer(element: NodElement): String {
        return element.telefonnummer
    }
}