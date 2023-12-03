package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.PlayerSettingsViewModel
import fr.harmoniamk.statsmk.extension.isTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable

fun PlayersSettingsScreen(
    viewModel: PlayerSettingsViewModel = hiltViewModel(),
    canAdd: Boolean = false,
    ally: Boolean = false,
    onBack: () -> Unit
) {
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val currentState = viewModel.sharedBottomSheetValue.collectAsState(null)
    val players by viewModel.sharedPlayers.collectAsState()
    val dummies by viewModel.sharedPlayersWithoutAccount.collectAsState()
    val addPlayerVisible = viewModel.sharedAddPlayerVisibility.collectAsState()
    val teamId by viewModel.sharedTeamId.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    BackHandler { onBack() }
    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sharedDismiss.collect {
            onBack()
        }
    }
    MKBaseScreen(title = R.string.joueurs, state = bottomSheetState, sheetContent = {
        MKBottomSheet(
            trackIndex = null,
            state = currentState.value,
            onDismiss = viewModel::dismissBottomSheet,
            onEditPosition = {},
            onEditTrack = {}
        )
    }) {
        addPlayerVisible.value.takeIf { it }?.let {
            MKSegmentedButtons(
                buttons = listOf(
                    Pair(R.string.cr_er_un_joueur, viewModel::onCreatePlayer)
                )
            )
        }
        MKTextField(
            value = searchState.value,
            onValueChange = {
                searchState.value = it
                viewModel.onSearch(it.text)
            },
            placeHolderRes = R.string.rechercher_un_joueur
        )
        LazyColumn(modifier = Modifier.padding(vertical = 10.dp)) {
            players.takeIf { it.isNotEmpty() }?.filter { !canAdd || (canAdd && it.player?.allyTeams?.contains(teamId).isTrue) }.let {
                stickyHeader {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                            .background(color = colorResource(R.color.harmonia_dark))
                    ) {
                        MKText(
                            font = R.font.montserrat_bold,
                            fontSize = 18,
                            text = "Inscrits",
                            textColor = R.color.white
                        )
                    }
                }
                items(items = players) {
                    MKPlayerItem(
                        player = it.player,
                        editVisible = it.canEdit && !canAdd,
                        onRootClick = { viewModel.takeIf { canAdd }?.onAddToTeam(it.player, ally) },
                        onEditClick = viewModel::onEditPlayer
                    )
                }
                dummies.takeIf { it.isNotEmpty() }?.let {
                    stickyHeader {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                                .background(color = colorResource(R.color.harmonia_dark))
                        ) {
                            MKText(
                                font = R.font.montserrat_bold,
                                fontSize = 18,
                                text = "Fictifs",
                                textColor = R.color.white
                            )
                        }
                    }
                    items(items = dummies) {
                        MKPlayerItem(
                            player = it.player,
                            editVisible = it.canEdit && !canAdd,
                            onRootClick = { viewModel.takeIf { canAdd }?.onAddToTeam(it.player, ally) },
                            onEditClick = viewModel::onEditPlayer
                        )
                    }
                }
            }

        }
    }
}