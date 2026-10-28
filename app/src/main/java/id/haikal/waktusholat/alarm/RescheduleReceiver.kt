package id.haikal.waktusholat.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import id.haikal.waktusholat.WaktuSholatApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

abstract class RescheduleReceiver : BroadcastReceiver() {

    final override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                runCatching {
                    (context.applicationContext as? WaktuSholatApp)
                        ?.container
                        ?.alarmScheduler
                        ?.scheduleAll()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
