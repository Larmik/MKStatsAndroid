package fr.harmoniamk.statsmk.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.activity.splash.SplashScreenActivity
import fr.harmoniamk.statsmk.datasource.TeamLocalDataSource
import fr.harmoniamk.statsmk.datasource.TopicLocalDataSource
import fr.harmoniamk.statsmk.datasource.UserLocalDataSource
import fr.harmoniamk.statsmk.datasource.WarLocalDataSource
import fr.harmoniamk.statsmk.repository.DatabaseRepository
import fr.harmoniamk.statsmk.repository.FirebaseRepository
import fr.harmoniamk.statsmk.repository.PreferencesRepository
import fr.harmoniamk.statsmk.repository.RemoteConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MKWidgetProvider : AppWidgetProvider(), CoroutineScope {

    private lateinit var context: Context

    private val viewModel by lazy {
        MKWidgetViewModel(
            preferencesRepository = PreferencesRepository(context),
            firebaseRepository = FirebaseRepository(
                preferencesRepository = PreferencesRepository(context),
                databaseRepository = DatabaseRepository(
                    userDataSource = UserLocalDataSource(context),
                    teamDataSource = TeamLocalDataSource(context),
                    warDataSource = WarLocalDataSource(context),
                    topicDataSource = TopicLocalDataSource(context)
                ),
                remoteConfigRepository = RemoteConfigRepository()
            ),
            databaseRepository = DatabaseRepository(
                userDataSource = UserLocalDataSource(context),
                teamDataSource = TeamLocalDataSource(context),
                warDataSource = WarLocalDataSource(context),
                topicDataSource = TopicLocalDataSource(context)
            )
        )
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "fr.harmoniamk.statsmk.REFRESH_WIDGET"
        const val CLICK_ACTION = "fr.harmoniamk.statsmk.EMPTY_LIST_CLICK"
        const val DATA_FETCHED = "fr.harmoniamk.statsmk.DATA_FETCHED"
        const val ERROR = "fr.harmoniamk.statsmk.ERROR"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        this.context = context
        appWidgetIds.forEach {
            updateWidget(
                context = context,
                id = it,
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        this.context = context
        val widgetID = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val receiver = IntentFilter(AppWidgetManager.EXTRA_APPWIDGET_ID)
        context.applicationContext?.registerReceiver(this, receiver)

        when (intent?.action) {
            ACTION_REFRESH_WIDGET, AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                updateWidget(context = context, id = widgetID)
                startRequest(context = context, appWidgetId = widgetID)
            }
            CLICK_ACTION -> launchApplication(context = context)
            else -> updateWidget(context = context, id = widgetID)
        }
    }

    private fun launchApplication(context: Context?) {
        val intent = Intent(context, SplashScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("openCurrentWar", true)
        context?.startActivity(intent)
    }

    private fun startRequest(context: Context, appWidgetId: Int) {
        this.context = context
        val serviceIntent = Intent(context, MKWidgetFetchService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> context.startForegroundService(serviceIntent)
            else -> context.startService(serviceIntent)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun updateWidget(context: Context, id: Int) {
        viewModel.currentWar
            .onEach {
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                val remainingMaps = 12 - (it?.warTracks?.size ?: 0)
                views.setTextViewText(R.id.widget_team_name, it?.name?.split("-")?.getOrNull(0)?.trim().toString())
                views.setTextViewText(R.id.widget_opponent_name, it?.name?.split("-")?.getOrNull(1)?.trim().toString())
                views.setTextViewText(R.id.widget_team_score, it?.displayedScore?.split("-")?.getOrNull(0)?.trim().toString())
                views.setTextViewText(R.id.widget_opponent_score, it?.displayedScore?.split("-")?.getOrNull(1)?.trim().toString())
                views.setTextViewText(R.id.widget_war_diff, it?.displayedDiff)
                views.setTextViewText(R.id.widget_remaining_maps, remainingMaps.toString())
                AppWidgetManager.getInstance(context).updateAppWidget(id, views)
            }.launchIn(this)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}
