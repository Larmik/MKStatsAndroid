package fr.harmoniamk.statsmk.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.application.MainApplication
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.databinding.ActivityMainBinding
import fr.harmoniamk.statsmk.repository.PreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlin.system.exitProcess

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val navController by lazy { findNavController(R.id.main_activity_fragment) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferencesRepository(this)
        this.setTheme(preferences.currentTheme)
        setContentView(this.binding.root)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        val destination = if (preferences.currentUser != null) R.id.homeFragment else R.id.welcomeFragment
        navGraph.startDestination = destination
        navController.graph = navGraph
    }

    override fun onDestroy() {
        super.onDestroy()
        //exitProcess(0)
    }

}