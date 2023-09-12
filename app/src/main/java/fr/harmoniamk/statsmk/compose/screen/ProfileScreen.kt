package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
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
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.ProfileViewModel
import fr.harmoniamk.statsmk.enums.MenuItems
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(), onLogout: () -> Unit) {

    val picture = viewModel.sharedPictureLoaded.collectAsState()
    val name = viewModel.sharedName.collectAsState()
    val email = viewModel.sharedEmail.collectAsState()
    val team = viewModel.sharedTeam.collectAsState()
    val fc = viewModel.sharedFriendCode.collectAsState()
    val role = viewModel.sharedRole.collectAsState()
    val localPicture = viewModel.sharedLocalPicture.collectAsState()
    val currentState = viewModel.sharedBottomSheetValue.collectAsState(null)
    val dialogState = viewModel.sharedDialogValue.collectAsState(null)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
        onResult = viewModel::onPictureEdited
    )
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

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
            AsyncImage(
                model = picture.value, contentDescription = null, modifier = Modifier
                    .size(170.dp)
                    .padding(top = 15.dp)
                    .clickable { viewModel.onTestFilter() }
            )
            localPicture.value?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(170.dp)
                        .padding(top = 15.dp)
                )
            }
            MKText(
                text = name.value.orEmpty(),
                fontSize = 26,
                font = R.font.montserrat_bold,
                modifier = Modifier.padding(vertical = 15.dp)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(1.dp)
                    .background(color = colorResource(id = R.color.white))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp), horizontalAlignment = Alignment.Start
            ) {
                team.value?.let {
                    MKText(text = stringResource(id = R.string.equipe_actuelle))
                    MKText(
                        text = team.value.orEmpty(),
                        fontSize = 16,
                        font = R.font.montserrat_bold,
                        modifier = Modifier.padding(bottom = 15.dp)
                    )
                    role.value?.let {
                        MKText(text = stringResource(id = R.string.r_le))
                        MKText(
                            text = it,
                            fontSize = 16,
                            font = R.font.montserrat_bold,
                            modifier = Modifier.padding(bottom = 15.dp)
                        )

                    }
                }
                MKText(text = stringResource(id = R.string.adresse_mail))
                MKText(
                    text = email.value.orEmpty(),
                    fontSize = 16,
                    font = R.font.montserrat_bold,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                fc.value?.let {
                    MKText(text = stringResource(id = R.string.code_ami))
                    MKText(text = fc.value.orEmpty(), fontSize = 16, font = R.font.montserrat_bold)
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(1.dp)
                    .background(color = colorResource(id = R.color.white))
            )
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                listOf(
                    MenuItems.ChangePseudo(),
                    MenuItems.ChangeMail(),
                    MenuItems.ChangePassword(),
                    MenuItems.ChangePicture(),
                    MenuItems.LeaveTeam().takeIf { team.value != null },
                    MenuItems.Logout()
                ).filterNotNull()
                    .forEach {
                    MKListItem(item = it, separator = false, onNavigate = { }) {
                        when (it) {
                            is MenuItems.ChangePseudo -> viewModel.onEditNickname()
                            is MenuItems.ChangeMail -> viewModel.onEditEmail()
                            is MenuItems.ChangePassword -> viewModel.onEditPassword()
                            is MenuItems.ChangePicture -> {
                                launcher.launch(
                                    PickVisualMediaRequest(
                                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                            is MenuItems.LeaveTeam -> viewModel.onLeaveTeam()
                            is MenuItems.Logout -> viewModel.onLogout()
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}