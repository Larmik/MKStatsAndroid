package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.TeamSettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TeamSettingsScreen(viewModel: TeamSettingsViewModel = hiltViewModel()) {
    val picture = viewModel.sharedPictureLoaded.collectAsState()
    val teamName = viewModel.sharedTeamName.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val players by viewModel.sharedPlayers.collectAsState()
    val allies by viewModel.sharedAllies.collectAsState()
    val currentState = viewModel.sharedBottomSheetValue.collectAsState(null)
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val manageVisible = viewModel.sharedManageVisible.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
        onResult = viewModel::onPictureEdited
    )
    val topButtons = listOf(
        Pair(R.string.modifier_l_quipe, viewModel::onEditTeam),
        Pair(R.string.modifier_le_logo) {
            launcher.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        },
    )

    val bottomButtons = listOf(
        Pair(R.string.ajouter_un_joueur, { viewModel.onAddPlayer(false) }),
        Pair(R.string.ajouter_un_ally,{ viewModel.onAddPlayer(true) }),
    )

    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    MKBaseScreen(title = R.string.mon_quipe,
        subTitle = teamName.value.orEmpty(),
        state = bottomSheetState,
        sheetContent = {
            MKBottomSheet(
                trackIndex = null,
                state = currentState.value,
                onDismiss = viewModel::dismissBottomSheet,
                onEditPosition = {},
                onEditTrack = {}
            )
        }
    ) {
        manageVisible.value.takeIf { it }?.let {
            MKSegmentedButtons(buttons = topButtons)
        }
        when (picture.value) {
            null -> Image(
                painter = painterResource(R.drawable.mk_stats_logo_picture),
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )
            else ->  AsyncImage(
                model = picture.value,
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        manageVisible.value.takeIf { it }?.let {
            MKSegmentedButtons(buttons = bottomButtons)
        }
        MKTextField(
            modifier = Modifier.offset(y = (-7.5).dp).fillMaxWidth(),
                value = searchState.value,
        onValueChange = {
            searchState.value = it
            viewModel.onSearch(it.text)
        },
        placeHolderRes = R.string.rechercher_un_joueur
        )

        LazyColumn {
            players.takeIf { it.isNotEmpty() }?.let {
                stickyHeader {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(40.dp).background(color = colorResource(R.color.harmonia_dark))) {
                        MKText(font = R.font.montserrat_bold, fontSize = 18, text = "Roster", textColor = R.color.white)
                    }
                }
                items(items = players) {
                    MKPlayerItem(
                        player = it.player,
                        editVisible = it.canEdit,
                        onEditClick = viewModel::onEditPlayer
                    )
                }
            }

            allies.takeIf { it.isNotEmpty() }?.let {
                stickyHeader {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(40.dp).background(color = colorResource(R.color.harmonia_dark))) {
                        MKText(font = R.font.montserrat_bold, fontSize = 18, text = "Allies", textColor = R.color.white)
                    }
                }
                items(items = allies) {
                    MKPlayerItem(
                        player = it.player,
                        editVisible = it.canEdit,
                        onEditClick = viewModel::onEditPlayer
                    )
                }
            }

        }
    }
}