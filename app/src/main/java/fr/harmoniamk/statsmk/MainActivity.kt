package fr.harmoniamk.statsmk

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.database.model.User
import fr.harmoniamk.statsmk.databinding.ActivityMainBinding
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val navController by lazy { findNavController(R.id.main_activity_fragment) }
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainApplication.instance!!.applicationContext)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.binding.root)
        val isConnected = GsonBuilder().serializeNulls().create().fromJson(preferences.getString("currentUser", null), User::class.java) != null
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        val destination = if (isConnected) R.id.homeFragment else R.id.welcomeFragment
        navGraph.startDestination = destination
        navController.graph = navGraph
    }

    override fun onDestroy() {
        super.onDestroy()
        exitProcess(0)
    }

}