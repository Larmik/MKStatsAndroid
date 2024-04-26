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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.BuildConfig
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreditsScreen() {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    MKBaseScreen(title = R.string.credits) {
        MKText(text = stringResource(R.string.mario_kart_stats), font = R.font.montserrat_bold, fontSize = 24)
        MKText(text = stringResource(R.string.version, BuildConfig.VERSION_NAME), fontSize = 18)
        MKText(text = stringResource(R.string.made_with_by_larii), modifier = Modifier.padding(vertical = 20.dp))
        Column(Modifier.background(colorsViewModel.secondaryColorAlphaed)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ), horizontalAlignment = Alignment.Start
            ) {
                MKText(
                    text = stringResource(R.string.ressources),
                    fontSize = 20,
                    font = R.font.montserrat_bold,
                    modifier = Modifier.padding(top = 10.dp)
                )

                MKText(text = stringResource(R.string.mkcentral), fontSize = 18, modifier = Modifier.padding(vertical = 10.dp))
                MKText(text = stringResource(R.string.the_home_of_mario_kart_tournaments))
                MKText(text = stringResource(R.string.mkc_link))

                MKText(
                    text = stringResource(R.string.discordapi),
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = stringResource(R.string.api_discord_pour_les_d_veloppeurs))
                MKText(text = stringResource(R.string.discord_link))

                MKText(
                    text = stringResource(R.string.suite_firebase),
                    fontSize = 20,
                    font = R.font.montserrat_bold,
                    modifier = Modifier.padding(top = 10.dp)
                )

                MKText(
                    text = stringResource(R.string.firebase_realtime_database),
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = stringResource(R.string.base_de_donn_es_en_temps_r_el_via_google_cloud))
                MKText(text = stringResource(R.string.fb_real_link))

                MKText(
                    text = stringResource(R.string.firebase_authentication),
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = stringResource(R.string.portail_d_authentification_et_de_gestion_des_utilisateurs))
                MKText(text = stringResource(R.string.fb_auth_link))
                MKText(
                    text = stringResource(R.string.google),
                    fontSize = 20,
                    font = R.font.montserrat_bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
                MKText(
                    text = stringResource(R.string.google_cloud_platform),
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = stringResource(R.string.ex_cutions_automatiques_de_fonctions))
                MKText(text = stringResource(R.string.gcloud_link))
                MKText(
                    text = stringResource(R.string.google_play_billing),
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = stringResource(R.string.service_de_facturation_et_d_achat_in_app))
                MKText(text = stringResource(R.string.billing_link))
                MKText(
                    text = stringResource(R.string.librairies),
                    fontSize = 20,
                    font = R.font.montserrat_bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
               MKText(
                    text = stringResource(R.string.jetpack_compose),
                    fontSize = 18,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                MKText(text = stringResource(R.string.android_ui_framework))
                MKText(text = stringResource(R.string.compose_link))
                MKText(text = stringResource(R.string.room), fontSize = 18, modifier = Modifier.padding(vertical = 10.dp))
                MKText(text = stringResource(R.string.gestion_de_base_de_donn_e_locale))
                MKText(text = stringResource(R.string.room_link))
            }
        }
    }
}