package fr.harmoniamk.statsmk.activity.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.activity.MainActivity
import fr.harmoniamk.statsmk.databinding.ActivitySplashBinding
import fr.harmoniamk.statsmk.enums.WelcomeScreen
import fr.harmoniamk.statsmk.extension.isResumed
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }
    private val viewModel: SplashScreenViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java)
        this.setTheme(R.style.LaunchScreenTheme)
        this.setContentView(this.binding.root)
        viewModel.bind()
        viewModel.sharedWelcomeScreen
            .distinctUntilChanged()
            .onEach {
                intent.putExtra("screen", it.name)
                startActivity(intent)
                finish()
            }.launchIn(lifecycleScope)
        viewModel.sharedLoadingVisible
            .onEach {
                binding.loadingLabel.text = it
            }.launchIn(lifecycleScope)

        viewModel.sharedShowUpdatePopup
            .filter { lifecycle.isResumed }
            .onEach {
                val popup = PopupFragment("L'application nécessite une mise à jour pour fonctionner correctement. \n \n Veuillez mettre à jour l'application pour continuer.", positiveText = "Mettre à jour", negativeText = "Retour")
                popup.onNegativeClick.onEach { finish() }.launchIn(lifecycleScope)
                popup.onPositiveClick
                    .onEach {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                    }.launchIn(lifecycleScope)
                popup.takeIf { !it.isAdded }?.show(supportFragmentManager, null)
            }.launchIn(lifecycleScope)


        viewModel.sharedShowPopup
            .filter { lifecycle.isResumed }
            .onEach { pair ->
                val popup = when (pair.second.isEmpty()) {
                    true -> PopupFragment("Vous êtes hors connexion. \n \n Veuillez redémarrer l’application en étant connecté à Internet pour continuer")
                    else -> PopupFragment("Vous êtes hors connexion. \n \n Vous pourrez toujours consulter les wars et statistiques mais vous n'aurez pas accès à la war en cours ni à l'édition des joueurs et équipes.", negativeText = "Continuer")
                }
                binding.loadingLayout.isVisible = false
                intent.putExtra("screen", pair.first.name)
                popup.onNegativeClick
                    .onEach {
                        if (pair.second.isNotEmpty()) startActivity(intent)
                        finish()
                    }.launchIn(lifecycleScope)
                when (pair.first) {
                    WelcomeScreen.CONNECT, WelcomeScreen.WELCOME -> {
                        startActivity(intent)
                        finish()
                    }
                    else -> popup.takeIf { lifecycle.isResumed && !it.isAdded }?.show(supportFragmentManager, null)
                }
            }.launchIn(lifecycleScope)

    }
}