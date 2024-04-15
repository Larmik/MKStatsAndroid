package fr.harmoniamk.statsmk.compose.screen.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.BuildConfig
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKText

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreditsScreen() {
    MKBaseScreen(title = R.string.credits) {
        MKText(text = "Mario Kart Stats", font = R.font.montserrat_bold, fontSize = 24)
        MKText(text = "Version ${BuildConfig.VERSION_NAME}", fontSize = 18)
        MKText(text = "Made with ❤\uFE0F by Larii", modifier = Modifier.padding(vertical = 20.dp))
        Column(Modifier.background(colorResource(R.color.transparent_white))) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ), horizontalAlignment = Alignment.Start
            ) {
                MKText(
                    text = "Ressources",
                    fontSize = 20,
                    font = R.font.montserrat_bold,
                    modifier = Modifier.padding(top = 10.dp)
                )

                MKText(text = "MKCentral", fontSize = 18, modifier = Modifier.padding(vertical = 10.dp))
                MKText(text = "The home of Mario Kart tournaments")
                MKText(text = "https://www.mariokartcentral.com/mkc/")

                MKText(
                    text = "DiscordAPI",
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = "API Discord pour les développeurs")
                MKText(text = "https://discord.com/developers/docs/intro")

                MKText(
                    text = "Suite Firebase",
                    fontSize = 20,
                    font = R.font.montserrat_bold,
                    modifier = Modifier.padding(top = 10.dp)
                )

                MKText(
                    text = "Firebase Realtime Database",
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = "Base de données en temps réel via Google Cloud")
                MKText(text = "https://firebase.google.com/docs/database")

                MKText(
                    text = "Firebase Authentication",
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = "Portail d'authentification et de gestion des utilisateurs")
                MKText(text = "https://firebase.google.com/docs/auth")
                MKText(
                    text = "Google",
                    fontSize = 20,
                    font = R.font.montserrat_bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
                MKText(
                    text = "Google Cloud Platform",
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = "Exécutions automatiques de fonctions")
                MKText(text = "https://cloud.google.com/gcp/?hl=fr")
                MKText(
                    text = "Google Play Billing",
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = "Service de facturation et d'achat in-app")
                MKText(text = "https://developer.android.com/google/play/billing/?hl=fr")
                MKText(
                    text = "Librairies",
                    fontSize = 20,
                    font = R.font.montserrat_bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
               MKText(
                    text = "Jetpack Compose",
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = "Android UI framework")
                MKText(text = "https://developer.android.com/develop/ui/compose?hl=fr")
                MKText(text = "Room", fontSize = 18, modifier = Modifier.padding(vertical = 10.dp))
                MKText(text = "Gestion de base de donnée locale")
                MKText(text = "https://developer.android.com/training/data-storage/room?hl=fr")
            }
        }
    }
}