package id.haikal.waktusholat.alarm

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import id.haikal.waktusholat.MainActivity
import id.haikal.waktusholat.R
import id.haikal.waktusholat.domain.model.AdhanSound
import id.haikal.waktusholat.domain.model.Prayer

class NotificationHelper(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)
    private val notificationManagerCompat = NotificationManagerCompat.from(appContext)

    fun ensureChannels() {
        val channels = listOf(
            channel(
                id = CHANNEL_ADHAN_FULL,
                nameRes = R.string.notification_channel_adzan_full,
                descriptionRes = R.string.notification_channel_adzan_full_description,
                sound = rawSound(R.raw.adzan_full),
                importance = NotificationManager.IMPORTANCE_HIGH,
            ),
            channel(
                id = CHANNEL_ADHAN_SHORT,
                nameRes = R.string.notification_channel_adzan_short,
                descriptionRes = R.string.notification_channel_adzan_short_description,
                sound = rawSound(R.raw.adzan_short),
                importance = NotificationManager.IMPORTANCE_HIGH,
            ),
            channel(
                id = CHANNEL_REMINDER,
                nameRes = R.string.notification_channel_reminder,
                descriptionRes = R.string.notification_channel_reminder_description,
                sound = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                importance = NotificationManager.IMPORTANCE_DEFAULT,
            ),
            channel(
                id = CHANNEL_SILENT,
                nameRes = R.string.notification_channel_silent,
                descriptionRes = R.string.notification_channel_silent_description,
                sound = null,
                importance = NotificationManager.IMPORTANCE_LOW,
            ),
        )
        notificationManager.createNotificationChannels(channels)
    }

    fun showAdhan(
        prayer: Prayer,
        sound: AdhanSound,
        locationName: String?,
        timeText: String?,
    ) {
        val channelId = when (sound) {
            AdhanSound.FULL -> CHANNEL_ADHAN_FULL
            AdhanSound.SHORT -> CHANNEL_ADHAN_SHORT
            AdhanSound.DEFAULT -> CHANNEL_REMINDER
            AdhanSound.SILENT -> CHANNEL_SILENT
        }
        show(
            notificationId = ADHAN_NOTIFICATION_ID + prayer.ordinal,
            channelId = channelId,
            title = appContext.getString(R.string.notification_adhan_title, prayerLabel(prayer)),
            text = locationTimeText(locationName, timeText),
            priority = NotificationCompat.PRIORITY_HIGH,
            category = NotificationCompat.CATEGORY_ALARM,
        )
    }

    fun showReminder(
        prayer: Prayer,
        minutesBefore: Int,
        timeText: String?,
    ) {
        show(
            notificationId = REMINDER_NOTIFICATION_ID + prayer.ordinal,
            channelId = CHANNEL_REMINDER,
            title = appContext.getString(R.string.notification_reminder_title, prayerLabel(prayer)),
            text = appContext.getString(
                R.string.notification_reminder_text,
                minutesBefore,
                timeText ?: "",
            ),
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_REMINDER,
        )
    }

    @SuppressLint("MissingPermission")
    private fun show(
        notificationId: Int,
        channelId: String,
        title: String,
        text: String,
        priority: Int,
        category: String,
    ) {
        if (!canPostNotifications()) return

        val contentIntent = PendingIntent.getActivity(
            appContext,
            CONTENT_INTENT_REQUEST_CODE,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(category)
            .setPriority(priority)
            .build()

        runCatching { notificationManagerCompat.notify(notificationId, notification) }
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return notificationManagerCompat.areNotificationsEnabled()
    }

    private fun locationTimeText(locationName: String?, timeText: String?): String = when {
        locationName != null && timeText != null -> appContext.getString(
            R.string.notification_location_time,
            locationName,
            timeText,
        )

        timeText != null -> timeText
        else -> ""
    }

    private fun prayerLabel(prayer: Prayer): String {
        val resId = when (prayer) {
            Prayer.IMSAK -> R.string.prayer_imsak
            Prayer.SUBUH -> R.string.prayer_subuh
            Prayer.TERBIT -> R.string.prayer_terbit
            Prayer.DZUHUR -> R.string.prayer_dzuhur
            Prayer.ASHAR -> R.string.prayer_ashar
            Prayer.MAGHRIB -> R.string.prayer_maghrib
            Prayer.ISYA -> R.string.prayer_isya
        }
        return appContext.getString(resId)
    }

    private fun rawSound(@androidx.annotation.RawRes resId: Int): Uri =
        Uri.parse("android.resource://${appContext.packageName}/$resId")

    private fun channel(
        id: String,
        @StringRes nameRes: Int,
        @StringRes descriptionRes: Int,
        sound: Uri?,
        importance: Int,
    ): NotificationChannel = NotificationChannel(id, appContext.getString(nameRes), importance).apply {
        description = appContext.getString(descriptionRes)
        enableVibration(true)
        if (sound != null) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            setSound(sound, attributes)
        } else {
            setSound(null, null)
        }
    }

    companion object {
        const val CHANNEL_ADHAN_FULL = "adzan_full"
        const val CHANNEL_ADHAN_SHORT = "adzan_short"
        const val CHANNEL_REMINDER = "reminder"
        const val CHANNEL_SILENT = "silent"

        private const val ADHAN_NOTIFICATION_ID = 1
        private const val REMINDER_NOTIFICATION_ID = 100
        private const val CONTENT_INTENT_REQUEST_CODE = 1
    }
}
