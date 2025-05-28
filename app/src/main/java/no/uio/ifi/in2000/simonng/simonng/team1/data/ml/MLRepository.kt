package no.uio.ifi.in2000.simonng.simonng.team1.data.ml

import android.content.Context
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker

open class MLRepository(kontekst: Context?, var testModus: Boolean = false) {

    private val mlModell = kontekst?.let { MLDataSource(it) }
    private val testVerdi = 3f

    init {
        if (!testModus && kontekst != null) {
            mlModell?.initialiser()
        }
    }

    fun predikerUlykker(
        fylke: String,
        dato: String,
        nedboer: Float,
        temperatur: Float
    ): Float {
        val storFylke = fylke.uppercase()

        val gyldigFylke = try {
            val fylkeNavn = storFylke.replace("Ø", "OE").replace("Æ", "AE").replace("Å", "AA")
            Fylker.valueOf(fylkeNavn)
            true
        } catch (e: IllegalArgumentException) {
            false
        }

        if (!gyldigFylke) {
            return -1f
        }

        if (testModus) {
            return testVerdi
        }

        if (mlModell == null) {
            return -2f
        }

        val datoObjekt = mlModell.parseDato(dato) ?: return -3f

        return mlModell.predikerAntallUlykker(
            fylke = storFylke,
            dato = datoObjekt,
            temperatur = temperatur,
            nedbor = nedboer
        )
    }

    fun lukk() {
        if (!testModus) {
            mlModell?.lukk()
        }
    }
}
