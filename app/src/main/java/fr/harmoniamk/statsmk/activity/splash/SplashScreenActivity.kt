package fr.harmoniamk.statsmk.activity.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.activity.MainActivity
import fr.harmoniamk.statsmk.databinding.ActivitySplashBinding
import fr.harmoniamk.statsmk.repository.PreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
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
        val preferences = PreferencesRepository(this)
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
    }
}