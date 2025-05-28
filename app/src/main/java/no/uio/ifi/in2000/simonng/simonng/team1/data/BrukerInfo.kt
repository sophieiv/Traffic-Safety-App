package no.uio.ifi.in2000.simonng.simonng.team1.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Tilleggsegenskap for Context for å opprette en DataStore
private val Context.brukerDataStore: DataStore<Preferences> by preferencesDataStore(name = "bruker_preferanser")

class BrukerInfo(private val context: Context) {

    // Hva appen skal huske
    companion object {
        private val BRUKER_NAVN_NOKKEL = stringPreferencesKey("bruker_navn")
        private val APP_BRUKT_FOR = booleanPreferencesKey("app_brukt_for")
        private val MORK_MODUS = booleanPreferencesKey("mork_modus")
        private val VALGT_FYLKE = stringPreferencesKey("valgt_fylke")
    }

    init {
        runBlocking {
            try {
                val preferences = context.brukerDataStore.data.first()
                if (!preferences.contains(APP_BRUKT_FOR)) {
                    context.brukerDataStore.edit { prefs ->
                        prefs[APP_BRUKT_FOR] = false
                    }
                    Log.d("BrukerInfo", "Initialisert APP_BRUKT_FOR til false")
                }
                // Initialiser mørk modus-preferansen hvis den ikke finnes
                if (!preferences.contains(MORK_MODUS)) {
                    context.brukerDataStore.edit { prefs ->
                        prefs[MORK_MODUS] = false // Standard er lys modus
                    }
                    Log.d("BrukerInfo", "Initialisert MORK_MODUS til false")
                }
                // Initialiser valgt fylke hvis det ikke finnes
                if (!preferences.contains(VALGT_FYLKE)) {
                    context.brukerDataStore.edit { prefs ->
                        prefs[VALGT_FYLKE] = Fylker.OSLO.name // Standard er Oslo
                    }
                    Log.d("BrukerInfo", "Initialisert VALGT_FYLKE til OSLO")
                } else {
                    Log.d("BrukerInfo", "Feil ved initialisering: VALGT_FYLKE finnes allerede")
                }
            } catch (e: Exception) {
                Log.e("BrukerInfo", "Feil ved initialisering: ${e.message}", e)
            }
        }
    }

    // Lagrer brukernavn
    suspend fun lagreBrukerNavn(navn: String) {
        try {
            context.brukerDataStore.edit { preferanser ->
                preferanser[BRUKER_NAVN_NOKKEL] = navn
                preferanser[APP_BRUKT_FOR] = true
            }
            Log.d("BrukerInfo", "Brukernavn lagret: $navn")
        } catch (e: Exception) {
            Log.e("BrukerInfo", "Feil ved lagring av brukernavn: ${e.message}", e)
        }
    }

    // Lagrer mørk modus-preferanse
    suspend fun lagreMorkModus(aktivert: Boolean) {
        try {
            context.brukerDataStore.edit { preferanser ->
                preferanser[MORK_MODUS] = aktivert
            }
            Log.d("BrukerInfo", "Mørk modus preferanse lagret: $aktivert")
        } catch (e: Exception) {
            Log.e("BrukerInfo", "Feil ved lagring av mørk modus: ${e.message}", e)
        }
    }

    // Lagrer valgt fylke
    suspend fun lagreValgtFylke(fylke: Fylker) {
        try {
            context.brukerDataStore.edit { preferanser ->
                preferanser[VALGT_FYLKE] = fylke.name
            }
            Log.d("BrukerInfo", "Valgt fylke lagret: ${fylke.name}")
        } catch (e: Exception) {
            Log.e("BrukerInfo", "Feil ved lagring av valgt fylke: ${e.message}", e)
        }
    }

    // Markerer om brukeren har brukt appen før
    suspend fun setBrukerHarIkkeBruktAppFor() {
        try {
            context.brukerDataStore.edit { preferanser ->
                preferanser[APP_BRUKT_FOR] = true
            }
            Log.d("BrukerInfo", "App markert som brukt")
        } catch (e: Exception) {
            Log.e("BrukerInfo", "Feil ved markering av app som brukt: ${e.message}", e)
        }
    }

    // Henter brukernavn som Flow
    val brukerNavnFlow: Flow<String> = context.brukerDataStore.data
        .catch { exception ->
            Log.e("BrukerInfo", "Feil ved lesing av brukernavn: ${exception.message}", exception)
            emit(emptyPreferences())
        }
        .map { preferanser ->
            val navn = preferanser[BRUKER_NAVN_NOKKEL] ?: ""
            Log.d("BrukerInfo", "Hentet brukernavn: $navn")
            navn
        }

    // Henter om det er forste gang appen brukes
    val erForsteGangFlow: Flow<Boolean> = context.brukerDataStore.data
        .catch { exception ->
            Log.e("BrukerInfo", "Feil ved lesing av forste gang status: ${exception.message}", exception)
            emit(emptyPreferences())
        }
        .map { preferanser ->
            val erForsteGang = !(preferanser[APP_BRUKT_FOR] ?: false)
            Log.d("BrukerInfo", "Er FORste gang: $erForsteGang")
            erForsteGang
        }

    // Henter mørk modus-preferanse som Flow
    val morkModusFlow: Flow<Boolean> = context.brukerDataStore.data
        .catch { exception ->
            Log.e("BrukerInfo", "Feil ved lesing av mørk modus: ${exception.message}", exception)
            emit(emptyPreferences())
        }
        .map { preferanser ->
            val morkModus = preferanser[MORK_MODUS] ?: false
            Log.d("BrukerInfo", "Hentet mørk modus: $morkModus")
            morkModus
        }

    // Henter valgt fylke som Flow
    val valgtFylkeFlow: Flow<Fylker> = context.brukerDataStore.data
        .catch { exception ->
            Log.e("BrukerInfo", "Feil ved lesing av valgt fylke: ${exception.message}", exception)
            emit(emptyPreferences())
        }
        .map { preferanser ->
            val fylkeNavn = preferanser[VALGT_FYLKE] ?: Fylker.OSLO.name
            val fylke = try {
                Fylker.valueOf(fylkeNavn)
            } catch (e: IllegalArgumentException) {
                Log.e("BrukerInfo", "Ugyldig fylke: $fylkeNavn, bruker Oslo som standard")
                Fylker.OSLO
            }
            Log.d("BrukerInfo", "Hentet valgt fylke: ${fylke.name}")
            fylke
        }
}
