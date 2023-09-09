package fr.harmoniamk.statsmk.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.compose.screen.root.RootScreen


@OptIn(ExperimentalMaterialApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra("screen")?.let {
              setContent {
                  RootScreen(startDestination = it) { finish() }
              }
        }
    }
    
}