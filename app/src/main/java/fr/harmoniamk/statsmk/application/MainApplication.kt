package fr.harmoniamk.statsmk.application

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : MultiDexApplication() {

    companion object {
        var instance: MainApplication? = null
        var mRequestQueue: RequestQueue? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        mRequestQueue = Volley.newRequestQueue(instance?.applicationContext)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}