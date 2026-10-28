package id.haikal.waktusholat

import android.app.Application
import id.haikal.waktusholat.di.AppContainer

class WaktuSholatApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notificationHelper.ensureChannels()
    }
}
