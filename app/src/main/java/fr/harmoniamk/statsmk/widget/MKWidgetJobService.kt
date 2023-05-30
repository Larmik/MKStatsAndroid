package fr.harmoniamk.statsmk.widget

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.IntentFilter
import androidx.work.Configuration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class MKWidgetJobService: JobService() {

    init {
        val builder = Configuration.Builder()
        builder.setJobSchedulerJobIdRange(100, 1000)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        applicationContext.registerReceiver(MKWidgetProvider(), IntentFilter(MKWidgetProvider.DATA_FETCHED))
        val serviceIntent = Intent(this, MKWidgetFetchService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, params?.extras?.getLong("appWidgetId"))
        applicationContext.startService(serviceIntent)
        onStartCommand(serviceIntent, Service.START_FLAG_RETRY, 1)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

}