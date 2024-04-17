package fr.harmoniamk.statsmk.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.compose.screen.root.RootScreen
import fr.harmoniamk.statsmk.compose.ui.MKDialog
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@OptIn(ExperimentalMaterialApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val colorsViewModel: ColorsViewModel by viewModels()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        splashscreen.setKeepOnScreenCondition { true }
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel.bind()
        viewModel.connectBillingClient()
        MobileAds.initialize(this) { }
        viewModel.sharedWelcomeScreen
            .distinctUntilChanged()
            .onEach {
                splashscreen.setKeepOnScreenCondition { false }
                setContent {
                    val systemUiController = rememberSystemUiController()
                    systemUiController.setSystemBarsColor(
                        color = colorsViewModel.secondaryColor
                    )
                    RootScreen(startDestination = it.name) { finish() }
                }
            }.launchIn(lifecycleScope)

        viewModel.sharedDialogValue
            .onEach { state ->
                setContent{
                    state?.let {  MKDialog(state = it) }
                }
            }.launchIn(lifecycleScope)

        viewModel.sharedShowUpdatePopup
            .distinctUntilChanged()
            .onEach {
                setContent { MKDialog(MKDialogState.NeedsUpdate(
                    onUpdate = { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))) },
                    onDismiss = { finish() }
                )) }
            }.launchIn(lifecycleScope)

    }
    
}