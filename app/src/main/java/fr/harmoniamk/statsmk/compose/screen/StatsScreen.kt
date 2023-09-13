package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedSelector
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.ui.stats.MKMapsStatsView
import fr.harmoniamk.statsmk.compose.ui.stats.MKPlayerScoreStatsView
import fr.harmoniamk.statsmk.compose.ui.stats.MKTeamScoreStatView
import fr.harmoniamk.statsmk.compose.ui.stats.MKTeamStatsView
import fr.harmoniamk.statsmk.compose.ui.stats.MKWarDetailsStatsView
import fr.harmoniamk.statsmk.compose.ui.stats.MKWarStatsView
import fr.harmoniamk.statsmk.compose.viewModel.StatsType
import fr.harmoniamk.statsmk.compose.viewModel.StatsViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.model.local.Stats
import kotlinx.coroutines.flow.filterNotNull

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel(), type: StatsType, onDetailsClick: (StatsType) -> Unit) {
    val stats = viewModel.sharedStats.collectAsState()
    val subtitle = viewModel.sharedSubtitle.collectAsState()

    viewModel.init(type, true)

    LaunchedEffect(Unit) {
        viewModel.sharedDetailsClick.filterNotNull().collect {
            onDetailsClick(type)
        }
    }
    MKBaseScreen(title = type.title, subTitle = subtitle.value) {
        (type as? StatsType.MapStats)?.trackIndex?.let {
            MKTrackItem(map = Maps.values().getOrNull(it))
        }
        (type as? StatsType.PeriodicStats)?.let {
            MKSegmentedSelector(buttons = listOf(
                Pair(stringResource(id = R.string.hebdo)) { viewModel.init(type, true) },
                Pair(stringResource(id = R.string.mensuel)) { viewModel.init(type, false) },
            ))
        }
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            stats.value?.let { MKWarStatsView(mkStats = it) }
            type.takeIf { it is StatsType.IndivStats || (it as? StatsType.OpponentStats)?.userId != null }?.let {
                (stats.value as? Stats) ?.let { MKPlayerScoreStatsView(stats = it) }
            }
            stats.value?.let { MKWarDetailsStatsView(mkStats = it, type = type) }
            type.takeIf { it is StatsType.IndivStats || it is StatsType.TeamStats ||it is StatsType.PeriodicStats }?.let {
                (stats.value as? Stats) ?.let {
                    MKTeamStatsView(stats = it)
                }
            }
            stats.value?.let { MKTeamScoreStatView(stats = it) }
            type.takeIf { it !is StatsType.MapStats }?.let {
                (stats.value as? Stats) ?.let { MKMapsStatsView(stats = it, type = type) }
            }
            stats.value?.takeIf { type is StatsType.IndivStats || type is StatsType.OpponentStats || type is StatsType.MapStats }?.let {
                MKButton(text = R.string.voir_le_d_tail) {
                    viewModel.onDetailsClick()
                }
            }
        }
    }
}