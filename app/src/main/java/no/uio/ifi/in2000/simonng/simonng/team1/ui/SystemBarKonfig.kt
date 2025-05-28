@file:Suppress("DEPRECATION") //Nødvendig for å støtte API 26+ med legacy system UI håndtering

package no.uio.ifi.in2000.simonng.simonng.team1.ui

import android.app.Activity
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import no.uio.ifi.in2000.simonng.simonng.team1.ui.theme.MorkeBlaa


// Gjør at man kan "sveipe" tilbake dersom man f.eks. blir tatt til telefon-skjermen ved ulykke.
@Composable
fun SystemBarsKonfig(
    statusBarColor: Color = MorkeBlaa,
    darkStatusBarIcons: Boolean = false,
    hideNavigationBar: Boolean = true
) {
    val view = LocalView.current
    val context = LocalContext.current

    DisposableEffect(statusBarColor, darkStatusBarIcons, hideNavigationBar) {
        val window = (context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        window.statusBarColor = statusBarColor.toArgb()

        insetsController.isAppearanceLightStatusBars = darkStatusBarIcons

        if (hideNavigationBar) {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Skjuler kun navigasjonsbar, ikke statusbar
            insetsController.let { controller ->
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            // Gjemmer navigasjonsbar fra OS etter 3 sekunder
            val decorView = window.decorView
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if ((visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    view.postDelayed({
                        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                    }, 3000)
                }
            }
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }

        onDispose {
            // Gjenoppretter normal tilstand
            WindowCompat.setDecorFitsSystemWindows(window, true)
            if (hideNavigationBar) {
                insetsController.show(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }
}