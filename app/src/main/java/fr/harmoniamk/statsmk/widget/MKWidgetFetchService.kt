package fr.harmoniamk.statsmk.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MKWidgetFetchService : Service(), CoroutineScope {

    @Inject
    lateinit var firebaseRepository: FirebaseRepositoryInterface
    @Inject
    lateinit var preferencesRepository: PreferencesRepositoryInterface
    @Inject
    lateinit var databaseRepository: DatabaseRepositoryInterface

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground()
        else startForeground(34, Notification())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val notifId = "refresh_widget_notification"
        val chan = NotificationChannel(notifId, "Refresh widget", NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, notifId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.mk_stats_logo_picture)
            .setContentTitle(getString(R.string.app_name))
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(33, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        firebaseRepository.listenToCurrentWar()
            .onEach { populateWidget(it) }
            .launchIn(this)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun populateWidget(item: MKWar?) {
        val widgetManager = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(ComponentName(this, MKWidgetProvider::class.java))
            .toList()

        widgetManager.forEach { widgetId ->
            val updateIntent = Intent(this, MKWidgetProvider::class.java)
            updateIntent.action = when (item) {
                null -> MKWidgetProvider.ERROR
                else -> MKWidgetProvider.DATA_FETCHED
            }
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            sendBroadcast(updateIntent)
        }
        stopSelf()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
}
