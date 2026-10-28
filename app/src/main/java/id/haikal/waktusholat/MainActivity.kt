package id.haikal.waktusholat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import id.haikal.waktusholat.ui.navigation.AppNavHost
import id.haikal.waktusholat.ui.theme.WaktuSholatTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val syncCoordinator = (application as WaktuSholatApp).container.alarmSyncCoordinator
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { syncCoordinator.observeChanges() }
                syncCoordinator.syncOnOpen()
            }
        }

        setContent {
            WaktuSholatTheme {
                AppNavHost()
            }
        }
    }
}
