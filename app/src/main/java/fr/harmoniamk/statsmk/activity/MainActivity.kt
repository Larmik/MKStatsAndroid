package fr.harmoniamk.statsmk.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.ActivityMainBinding
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val navController by lazy { findNavController(R.id.main_activity_fragment) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.binding.root)
        //PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isConnected", false).apply()
        val isConnected = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isConnected", false)
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