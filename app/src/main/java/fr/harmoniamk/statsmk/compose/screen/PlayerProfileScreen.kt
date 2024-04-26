package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKProgress
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.compose.viewModel.PlayerProfileViewModel.Companion.viewModel
import fr.harmoniamk.statsmk.extension.displayedString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun PlayerProfileScreen(id: String) {

    val viewModel = viewModel(id = id)
    val email = viewModel.sharedEmail.collectAsState()
    val player = viewModel.sharedPlayer.collectAsState()
    val allyButton = viewModel.sharedAllyButton.collectAsState()
    val role = viewModel.sharedRole.collectAsState()
    val adminButton = viewModel.sharedAdminButton.collectAsState()

    val colorsViewModel: ColorsViewModel = hiltViewModel()
    MKBaseScreen(title = R.string.profil) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            when (player.value) {
                null ->  MKProgress()
                else -> {
                    when (player.value?.profile_picture?.takeIf { it.isNotEmpty() }) {
                        null -> Image(
                            painter = painterResource(R.drawable.mk_stats_logo_picture),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                        else -> AsyncImage(
                            model = player.value?.profile_picture, contentDescription = null, modifier = Modifier
                                .size(170.dp)
                                .padding(top = 15.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = player.value?.flag,
                            contentDescription = null,
                            modifier = Modifier
                                .width(40.dp)
                                .height(30.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.FillBounds
                        )
                        Spacer(Modifier.width(20.dp))
                        MKText(
                            text = player.value?.display_name.orEmpty(),
                            fontSize = 26,
                            font = R.font.montserrat_bold,
                            modifier = Modifier.padding(vertical = 15.dp)
                        )
                    }

                    player.value?.profile_message?.let {
                        MKText(
                            text = it,
                            modifier = Modifier.padding(vertical = 15.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(1.dp)
                            .background(color = colorsViewModel.mainTextColor)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp), horizontalAlignment = Alignment.Start
                    ) {
                        player.value?.createdDate?.let {
                            MKText(text = stringResource(id = R.string.inscrit_depuis))
                            MKText(
                                text = it.displayedString(stringResource(R.string.full_date_format)),
                                fontSize = 16,
                                font = R.font.montserrat_bold,
                                modifier = Modifier.padding(bottom = 15.dp)
                            )
                        }
                        player.value?.current_teams?.firstOrNull().let {
                            MKText(text = stringResource(id = R.string.equipe_actuelle))
                            MKText(
                                text = it?.team_name.orEmpty(),
                                fontSize = 16,
                                font = R.font.montserrat_bold,
                                modifier = Modifier.padding(bottom = 15.dp)
                            )
                        }

                        email.value?.let {
                            MKText(text = stringResource(id = R.string.adresse_mail))
                            MKText(
                                text = it,
                                fontSize = 16,
                                font = R.font.montserrat_bold,
                                modifier = Modifier.padding(bottom = 15.dp)
                            )
                        }
                        player.value?.switch_fc?.let {
                            MKText(text = stringResource(id = R.string.code_ami))
                            MKText(text = it, fontSize = 16, font = R.font.montserrat_bold,
                                modifier = Modifier.padding(bottom = 15.dp)
                            )
                        }
                        player.value?.discord_tag?.let {
                            MKText(text = stringResource(id = R.string.discord_tag))
                            MKText(text = it, fontSize = 16, font = R.font.montserrat_bold,
                                modifier = Modifier.padding(bottom = 15.dp)
                            )
                        }
                        role.value?.let {
                            MKText(text = stringResource(id = R.string.r_le))
                            MKText(text = it, fontSize = 16, font = R.font.montserrat_bold)
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(1.dp)
                            .background(color = colorsViewModel.mainTextColor)
                    )
                    allyButton.value?.let {
                        when (it.second) {
                            true ->   MKButton(text = stringResource(R.string.ajouter_en_tant_qu_ally), onClick = viewModel::onAddAlly)
                            else ->   MKText(text = stringResource(R.string.ce_joueur_est_un_ally), modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                    adminButton.value?.let {
                        when (it) {
                            true -> MKButton(text = stringResource(R.string.basculer_en_tant_que_membre), onClick ={ viewModel.onAdmin(false) })
                            else -> MKButton(text = stringResource(R.string.basculer_en_tant_qu_admin), onClick ={ viewModel.onAdmin(true) })
                        }
                    }
                }
            }
        }
    }
}