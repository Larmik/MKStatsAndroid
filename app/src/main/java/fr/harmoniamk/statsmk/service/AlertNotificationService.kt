package fr.harmoniamk.statsmk.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.activity.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import java.util.*


@FlowPreview
@ExperimentalCoroutinesApi
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class AlertNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
            val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val showIntent = Intent(this, MainActivity::class.java)
            showIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            showIntent.action = System.currentTimeMillis().toString()
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(this, 0, showIntent, flags)
            val channelId = getString(R.string.default_notification_channel_id)
            val mBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.mk_stats_logo_picture)
                .setContentTitle(remoteMessage.notification?.title)
                .setContentText(remoteMessage.notification?.body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(remoteMessage.sentTime)
                .setVibrate(longArrayOf(0, 500))
                .setDefaults(Notification.DEFAULT_SOUND)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(NotificationChannel(channelId, "Notifications MK Stats", NotificationManager.IMPORTANCE_HIGH))
                mBuilder.setChannelId(channelId)
            }
            notificationManager.notify((Date().time / 1000L % Int.MAX_VALUE).toInt(), mBuilder.build())
    }

    val token: Flow<String> = callbackFlow {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && isActive) offer(task.result)
        }
        awaitClose {  }
    }

}