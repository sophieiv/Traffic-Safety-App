package no.uio.ifi.in2000.simonng.simonng.team1.data.ml

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import no.uio.ifi.in2000.simonng.simonng.team1.data.Fylker


class MLDataSource(private val kontekst: Context) {

    // Konstanter
    companion object {
        private const val TAGG = "MLDataSource"
        private const val MODELL_FIL = "ulykker_modell.tflite"
        private const val METADATA_FIL = "ulykker_modell_metadata.json"

        // Liste over fylker modellen støtter
        val GYLDIGE_FYLKER = Fylker.entries.map {
            it.name.replaceFirstChar{ char -> char.uppercase() }.replace("OESTFOLD", "ØSTFOLD")}
    }

    private var tolk: Interpreter? = null
    private var erInitialisert = false

    // Metadata som f.eks. vekter
    private var metadata: JSONObject? = null
    private var fylkeTilIndeks: Map<String, Int> = emptyMap()
    private var egenskapKolonner: List<String> = emptyList()

    // Normaliseringsverdier for inndataen
    private var nedborGjennomsnitt: Float = 0f
    private var nedborStandardavvik: Float = 1f
    private var tempGjennomsnitt: Float = 0f
    private var tempStandardavvik: Float = 1f


    //Setter opp modellen og laster inn nødvendig data
    fun initialiser() {
        try {
            // Laster inn modellen
            val modellFil = lastInnFil()
            tolk = Interpreter(modellFil)

            lastInnMetadata()

            erInitialisert = true

            Log.d(TAGG, "Modell klar til bruk")
        } catch (e: Exception) {
            Log.e(TAGG, "Kunne ikke sette opp modellen: ${e.message}")
        }
    }

    private fun erGyldigFylke(fylke: String): Boolean {
        return GYLDIGE_FYLKER.contains(fylke)
    }



    //Returnerer predikert antall eller -1/-4 ved feil
    fun predikerAntallUlykker(
        fylke: String,
        dato: Date,
        temperatur: Float,
        nedbor: Float
    ): Float {
        if (!erInitialisert) {
            initialiser()
        }
        if (!erGyldigFylke(fylke)) {
            Log.w(TAGG, "Ugyldig fylke: $fylke")
            return -1f
        }
        try {
            val inndata = lagInndata(fylke, dato, temperatur, nedbor)

            val resultat = utforPrediksjon(inndata)

            return resultat
        } catch (e: Exception) {
            Log.e(TAGG, "Feil under prediksjon: ${e.message}")
            return -4f
        }
    }

    fun parseDato(datoStreng: String): Date? {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return try {
            format.parse(datoStreng)
        } catch (e: Exception) {
            Log.e(TAGG, "Ugyldig datoformat: $datoStreng")
            null
        }
    }

    //Stopper unødvendige prosesser
    fun lukk() {
        tolk?.close()
        tolk = null
        metadata = null
        erInitialisert = false
    }

    //Tolker metadata-filen med info om modellen
    private fun lastInnMetadata() {
        val metadataStreng = kontekst.assets.open(METADATA_FIL)
            .bufferedReader()
            .use { it.readText() }

        metadata = JSONObject(metadataStreng)

        // Henter normaliseringsverdier (samme som modell ble trent på i python)
        metadata?.let { json ->
            nedborGjennomsnitt = json.getDouble("nedbor_mean").toFloat()
            nedborStandardavvik = json.getDouble("nedbor_std").toFloat()
            tempGjennomsnitt = json.getDouble("temp_mean").toFloat()
            tempStandardavvik = json.getDouble("temp_std").toFloat()

            // Henter egenskaper
            val egenskapTabell = json.getJSONArray("feature_columns")
            egenskapKolonner = List(egenskapTabell.length()) { egenskapTabell.getString(it) }

            // Henter fylkemapping
            val fylkeObjekt = json.getJSONObject("fylke_to_index")
            fylkeTilIndeks = fylkeObjekt.keys().asSequence().associateWith {
                fylkeObjekt.getInt(it)
            }
        }
    }

    //Laster fil fra assets-mappen
    private fun lastInnFil(): MappedByteBuffer {
        val filBeskrivelse = kontekst.assets.openFd(MODELL_FIL)
        val innDataStrom = FileInputStream(filBeskrivelse.fileDescriptor)
        val filKanal = innDataStrom.channel
        val startPosisjon = filBeskrivelse.startOffset
        val lengde = filBeskrivelse.declaredLength

        return filKanal.map(FileChannel.MapMode.READ_ONLY, startPosisjon, lengde)
    }

    private fun lagInndata(
        fylke: String,
        dato: Date,
        temperatur: Float,
        nedbor: Float
    ): FloatArray {
        if (!erInitialisert) {
            throw IllegalStateException("Modellen er ikke initialisert")
        }

        // Oppretter datatabell
        val inndata = FloatArray(egenskapKolonner.size)

        // Henter datoverdier
        val kalender = Calendar.getInstance().apply { time = dato }
        val dag = kalender.get(Calendar.DAY_OF_MONTH).toFloat()
        val maned = (kalender.get(Calendar.MONTH) + 1).toFloat()
        val ar = kalender.get(Calendar.YEAR).toFloat()
        val ukedag = kalender.get(Calendar.DAY_OF_WEEK).toFloat() - 2 // 0=mandag, 6=søndag

        // Normaliserer værdata
        val normalisertNedbor = (nedbor - nedborGjennomsnitt) / nedborStandardavvik
        val normalisertTemp = (temperatur - tempGjennomsnitt) / tempStandardavvik

        // Finner fylkeindeks
        val fylkeIndeks = fylkeTilIndeks[fylke] ?: -1

        // Setter verdier basert på kolonnerekkefølgen
        for (i in egenskapKolonner.indices) {
            when (egenskapKolonner[i]) {
                "Dag" -> inndata[i] = dag
                "Måned" -> inndata[i] = maned
                "År" -> inndata[i] = ar
                "Ukedag" -> inndata[i] = ukedag
                "Nedbør" -> inndata[i] = normalisertNedbor
                "Temp" -> inndata[i] = normalisertTemp
                "Fylke_Index" -> inndata[i] = fylkeIndeks.toFloat()
                else -> {
                    // One-hot encoding for fylker
                    if (egenskapKolonner[i].startsWith("Fylke_")) {
                        val fylkeNavn = egenskapKolonner[i].substring(6)
                        inndata[i] = if (fylkeNavn == fylke) 1f else 0f
                    }
                }
            }
        }
        return inndata
    }

    //Utfører prediksjon
    private fun utforPrediksjon(inndata: FloatArray): Float {
        // Konverter til ByteBuffer (kreves av TensorFlow)
        val inndataBuffer = ByteBuffer.allocateDirect(inndata.size * 4).apply {
            order(ByteOrder.nativeOrder())
            for (verdi in inndata) {
                putFloat(verdi)
            }
            rewind()
        }

        // Buffer for resultat
        val utdataBuffer = ByteBuffer.allocateDirect(4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Kjører modellen
        tolk?.run(inndataBuffer, utdataBuffer)

        // Henter resultatet
        utdataBuffer.rewind()
        return utdataBuffer.float
    }
}