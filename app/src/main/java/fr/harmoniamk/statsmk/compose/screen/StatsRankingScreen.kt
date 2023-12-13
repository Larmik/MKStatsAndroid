package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.local.RankingItemViewModel
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedSelector
import fr.harmoniamk.statsmk.compose.ui.MKTeamItem
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.StatsRankingState
import fr.harmoniamk.statsmk.compose.viewModel.StatsRankingViewModel
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.local.TrackStats
import kotlinx.coroutines.flow.filterNotNull

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatsRankingScreen(
    userId: String? = null,
    teamId: String? = null,
    state: StatsRankingState,
    goToStats: (RankingItemViewModel, String?, String?) -> Unit
) {
    val viewModel: StatsRankingViewModel = StatsRankingViewModel.viewModel(userId, teamId)
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val currentState = viewModel.sharedBottomSheetValue.collectAsState()
    val indiv = viewModel.sharedIndivEnabled.collectAsState()
    val newUserId = viewModel.sharedUserId.collectAsState()
    val newTeamId = viewModel.sharedTeamId.collectAsState()
    val bottomSheetState =
        rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            confirmValueChange = { it == ModalBottomSheetValue.Expanded || it == ModalBottomSheetValue.HalfExpanded })
    val list = viewModel.sharedList.collectAsState()

    viewModel.init(state, indiv.value)

    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sharedGoToStats.filterNotNull().collect {
           goToStats(it,  userId ?: newUserId.value, teamId ?: newTeamId.value)
        }
    }

    MKBaseScreen(title = state.title,
        subTitle = viewModel.sharedUserName.collectAsState().value,
        state = bottomSheetState,
        sheetContent = {
            MKBottomSheet(
                trackIndex = null,
                state = currentState.value,
                onDismiss = viewModel::dismissBottomSheet,
                onEditPosition = {},
                onEditTrack = {},
                onSorted = { viewModel.onSorted(state, it) }
            )
        }) {
        state.takeIf { it !is StatsRankingState.PlayerRankingState }?.let {
            if (userId == null)
                MKSegmentedSelector(
                    buttons = listOf(
                        Pair(stringResource(id = R.string.equipe)) { viewModel.init(it, false) },
                        Pair(stringResource(id = R.string.individuel)) { viewModel.init(it, true) }
                    ),
                    indexSelected = when (indiv.value) {
                        true -> 1
                        else -> 0
                    }
                )
        }
        Row(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MKTextField(
                modifier = Modifier.fillMaxWidth(0.8f),
                value = searchState.value,
                onValueChange = {
                    searchState.value = it
                    viewModel.onSearch(state, it.text)
                },
                placeHolderRes = state.placeholderRes
            )
            Image(modifier = Modifier
                .size(30.dp)
                .clickable { viewModel.onClickOptions(state) },
                painter = painterResource(id = R.drawable.listoption),
                contentDescription = null
            )
        }
        LazyColumn {
            items(list.value) { rankingItem ->
                when (rankingItem) {
                    is PlayerRankingItemViewModel -> {
                        MKPlayerItem(playerRanking = rankingItem, onRootClick = { viewModel.onItemClick(rankingItem) })
                    }

                    is OpponentRankingItemViewModel -> {
                        MKTeamItem(teamRanking = rankingItem, onClick = { viewModel.onItemClick(rankingItem) })
                    }

                    is TrackStats -> {
                        MKTrackItem(
                            modifier = Modifier.padding(bottom = 5.dp),
                            trackRanking = rankingItem,
                            onClick = {
                                viewModel.onItemClick(rankingItem)
                            }
                        )
                    }
                }
            }
        }
    }
}