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
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKDialog
import fr.harmoniamk.statsmk.compose.ui.MKListItem
import fr.harmoniamk.statsmk.compose.ui.MKProgress
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.compose.viewModel.ProfileViewModel
import fr.harmoniamk.statsmk.enums.MenuItems
import fr.harmoniamk.statsmk.extension.displayedString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(), onLogout: () -> Unit) {

    val email = viewModel.sharedEmail.collectAsState()
    val currentState = viewModel.sharedBottomSheetValue.collectAsState(null)
    val dialogState = viewModel.sharedDialogValue.collectAsState(null)

    val player = viewModel.sharedPlayer.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val colorsViewModel: ColorsViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.sharedDisconnect.collect {
            onLogout()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    dialogState.value?.let { MKDialog(state = it) }
    MKBaseScreen(title = R.string.profil,
        state = bottomSheetState,
        sheetContent = {
            MKBottomSheet(
                trackIndex = null,
                state = currentState.value,
                onDismiss = viewModel::dismissBottomSheet,
                onEditPosition = {},
                onEditTrack = {}
            )
        }) {
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
                            modifier = Modifier.size(120.dp).clip(CircleShape)
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
                            modifier = Modifier.size(30.dp).clip(CircleShape),
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
                            MKText(text = it, fontSize = 16, font = R.font.montserrat_bold)
                        }
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(1.dp)
                    .background(color = colorsViewModel.mainTextColor)
            )
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                listOf(
                    MenuItems.ChangeMail(),
                    MenuItems.ChangePassword(),
                    MenuItems.Logout()
                )
                    .forEach {
                    MKListItem(item = it, separator = false, onNavigate = { }) {
                        when (it) {
                            is MenuItems.ChangeMail -> viewModel.onEditEmail()
                            is MenuItems.ChangePassword -> viewModel.onEditPassword()
                            is MenuItems.Logout -> viewModel.onLogout()
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}