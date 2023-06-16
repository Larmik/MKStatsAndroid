package fr.harmoniamk.statsmk.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.activity.MainActivity
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

enum class NotifType(val titleId: Int, val messageId: Int) {
    dispo_create(R.string.notif_dispo_create_title, R.string.notif_dispo_create_message),
    lu_almost_ok(R.string.notif_lu_amost_ok_title, R.string.notif_lu_amost_ok_message),
    lu_ok(R.string.notif_lu_ok_title, R.string.notif_lu_ok_message),
    war_ok(R.string.notif_war_ok_title, R.string.notif_war_ok_message),
    war_reminder(R.string.notif_war_reminder_title, R.string.notif_war_reminder_message),
    dispo_reminder(R.string.notif_dispo_reminder_title, R.string.notif_dispo_reminder_message),
    dispo_delete(R.string.minus, R.string.minus)
}


@FlowPreview
@ExperimentalCoroutinesApi
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
@AndroidEntryPoint
class AlertNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var databaseRepository: DatabaseRepository
    @Inject
    lateinit var preferencesRepository: PreferencesRepository
    @Inject
    lateinit var authenticationRepository: AuthenticationRepository
    private val job = SupervisorJob()

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        CoroutineScope(job).launch {
            val scheduledHour = remoteMessage.data["hour"]
            val opponentId = remoteMessage.data["opponent"]
            val lineUp = remoteMessage.data["lu"] ?: ""
            val teamId = preferencesRepository.currentTeam?.mid
            val userId = authenticationRepository.user?.uid ?: ""
            val teamTag = databaseRepository.getTeam(teamId).firstOrNull()?.shortName ?: ""
            val opponentTag = databaseRepository.getTeam(opponentId).firstOrNull()?.shortName ?: ""
            remoteMessage.data["type"]?.let {
                val type = NotifType.valueOf(it)
                when (type) {
                    NotifType.dispo_create -> {
                        switchTopic(teamId + "_dispo_reminder_18", true)
                        switchTopic(teamId + "_dispo_reminder_20", true)
                        switchTopic(teamId + "_dispo_reminder_21", true)
                        switchTopic(teamId + "_dispo_reminder_22", true)
                        switchTopic(teamId + "_dispo_reminder_23", true)
                    }
                    NotifType.dispo_delete -> {
                        switchTopic(teamId + "_dispo_reminder_18", false)
                        switchTopic(teamId + "_dispo_reminder_20", false)
                        switchTopic(teamId + "_dispo_reminder_21", false)
                        switchTopic(teamId + "_dispo_reminder_22", false)
                        switchTopic(teamId + "_dispo_reminder_23", false)
                        switchTopic(teamId + "_war_reminder_18", false)
                        switchTopic(teamId + "_war_reminder_20", false)
                        switchTopic(teamId + "_war_reminder_21", false)
                        switchTopic(teamId + "_war_reminder_22", false)
                        switchTopic(teamId + "_war_reminder_23", false)
                        switchTopic(teamId + "_lu_infos_18", false)
                        switchTopic(teamId + "_lu_infos_20", false)
                        switchTopic(teamId + "_lu_infos_21", false)
                        switchTopic(teamId + "_lu_infos_22", false)
                        switchTopic(teamId + "_lu_infos_23", false)
                    }
                    NotifType.war_ok -> {
                        if (lineUp.contains(userId))
                            switchTopic(teamId + "_war_reminder_" + scheduledHour, true)
                        switchTopic(teamId + "_lu_infos_" + scheduledHour, false)
                    }
                    else -> {

                    }
                }
                type.takeIf { it != NotifType.dispo_delete }?.let { notifType ->
                    if ((notifType == NotifType.war_ok && lineUp.contains(userId)) || notifType != NotifType.war_ok)
                        sendNotification(notifType, "${scheduledHour}h", opponentTag, teamTag)
                }
            }
        }
    }

    private suspend fun switchTopic(topic: String, subscribed: Boolean) {
      switchNotification(topic, subscribed)
          .filterNotNull()
          .flatMapLatest {
              if (subscribed) databaseRepository.writeTopic(TopicEntity(topic))
                else databaseRepository.deleteTopic(topic)
          }.first()
    }

    private fun sendNotification(type: NotifType, hour: String, opponentTag: String, teamTag: String) {
        val notifTitle = when (type) {
            NotifType.war_reminder -> String.format(applicationContext.getString(type.titleId), hour, opponentTag)
            NotifType.dispo_reminder -> String.format(applicationContext.getString(type.titleId), hour)
            else -> applicationContext.getString(type.titleId)
        }
        val notifMessage = when (type) {
            NotifType.dispo_create -> String.format(applicationContext.getString(type.messageId), teamTag)
            NotifType.lu_almost_ok, NotifType.lu_ok -> String.format(applicationContext.getString(type.messageId), hour)
            NotifType.war_ok -> String.format(applicationContext.getString(type.messageId), hour, opponentTag)
            NotifType.war_reminder -> String.format(applicationContext.getString(type.messageId), opponentTag)
            else -> applicationContext.getString(type.messageId)
        }
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val showIntent = Intent(this@AlertNotificationService, MainActivity::class.java)
        showIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        showIntent.action = System.currentTimeMillis().toString()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this@AlertNotificationService, 0, showIntent, flags)
        val channelId = getString(R.string.default_notification_channel_id)
        val mBuilder = NotificationCompat.Builder(this@AlertNotificationService, channelId)
            .setSmallIcon(R.drawable.mk_stats_logo_picture)
            .setContentTitle(notifTitle)
            .setContentText(notifMessage)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
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
            if (task.isSuccessful && isActive) trySend(task.result)
        }
        awaitClose {  }
    }

     fun switchNotification(topic: String, subscribed: Boolean): Flow<Pair<String, Boolean>?> = callbackFlow<Pair<String, Boolean>?>{
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful && tokenTask.result.isNotEmpty())
                    FirebaseInstallations.getInstance().id.addOnCompleteListener { instanceTask ->
                        if (instanceTask.isSuccessful && instanceTask.result.isNotEmpty()) {
                            if (subscribed) FirebaseMessaging.getInstance().subscribeToTopic(topic)
                            else FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                            if (isActive) trySend(Pair(instanceTask.result, subscribed))
                        } else if (isActive) trySend(null)
                    }
                else if (isActive) trySend(null)
            }
        } catch (e: Exception) {
            if (isActive) trySend(null)
        }
        awaitClose { }

    }

}