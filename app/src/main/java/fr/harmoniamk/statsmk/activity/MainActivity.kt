package fr.harmoniamk.statsmk.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.ActivityMainBinding
import fr.harmoniamk.statsmk.enums.WelcomeScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val navController by lazy { findNavController(R.id.main_activity_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.binding.root)
        val screen = WelcomeScreen.getFromName(intent.getStringExtra("screen"))
        screen?.let {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.startDestination = it.fragmentResId
            navController.setGraph(navGraph, intent.extras)
        }
    }

}