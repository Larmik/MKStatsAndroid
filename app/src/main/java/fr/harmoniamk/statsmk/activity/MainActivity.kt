package fr.harmoniamk.statsmk.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.compose.screen.root.RootScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@OptIn(ExperimentalMaterialApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        splashscreen.setKeepOnScreenCondition { true }
        super.onCreate(savedInstanceState)
        viewModel.bind()
        viewModel.sharedWelcomeScreen
            .distinctUntilChanged()
            .onEach {
                splashscreen.setKeepOnScreenCondition { false }
                setContent { RootScreen(startDestination = it.name) { finish() } }
            }.launchIn(lifecycleScope)
    }
    
}