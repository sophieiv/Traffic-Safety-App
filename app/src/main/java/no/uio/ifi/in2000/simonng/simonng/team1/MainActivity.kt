package no.uio.ifi.in2000.simonng.simonng.team1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import no.uio.ifi.in2000.simonng.simonng.team1.data.navigasjon.HovedSkjerm
import no.uio.ifi.in2000.simonng.simonng.team1.ui.theme.Team1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Team1Theme {
                HovedSkjerm()
            }
        }
    }
}