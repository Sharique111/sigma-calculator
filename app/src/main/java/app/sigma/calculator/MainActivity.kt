package app.sigma.calculator

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import app.sigma.calculator.ui.CalculatorScreen
import app.sigma.calculator.ui.theme.SigmaTheme

/**
 * The app's entry point: Android starts here when you tap the Sigma icon.
 * It creates the ViewModel (which survives screen rotation) and shows the screen.
 */
class MainActivity : ComponentActivity() {

    private val vm: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // draw behind the status and navigation bars

        setContent {
            val systemDark = isSystemInDarkTheme()
            val dark = when (vm.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            // Make the status bar icons light or dark to match the app's theme.
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            SigmaTheme(dark = dark) {
                CalculatorScreen(vm = vm, dark = dark, onToggleTheme = { vm.toggleTheme(systemDark) })
            }
        }
    }
}
