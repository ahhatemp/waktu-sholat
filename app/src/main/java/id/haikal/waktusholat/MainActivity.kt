package id.haikal.waktusholat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.haikal.waktusholat.ui.navigation.AppNavHost
import id.haikal.waktusholat.ui.theme.WaktuSholatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WaktuSholatTheme {
                AppNavHost()
            }
        }
    }
}
