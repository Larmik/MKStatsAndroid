package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import fr.harmoniamk.statsmk.compose.ui.MKProgress
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedSelector
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.ui.stats.MKMapsStatsView
import fr.harmoniamk.statsmk.compose.ui.stats.MKPlayerScoreStatsView
import fr.harmoniamk.statsmk.compose.ui.stats.MKTeamScoreStatView
import fr.harmoniamk.statsmk.compose.ui.stats.MKTeamStatsView
import fr.harmoniamk.statsmk.compose.ui.stats.MKWarDetailsStatsView
import fr.harmoniamk.statsmk.compose.ui.stats.MKWarStatsView
import fr.harmoniamk.statsmk.compose.viewModel.Periodics
import fr.harmoniamk.statsmk.compose.viewModel.StatsType
import fr.harmoniamk.statsmk.compose.viewModel.StatsViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.model.local.Stats

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    type: StatsType,
    onWarDetailsClick: (StatsType, Boolean?) -> Unit,
    onTrackDetailsClick: (String?, String?, String?) -> Unit,
    goToWarDetails: (String?) -> Unit,
    goToOpponentStats: (String?, String?) -> Unit,
    goToMapStats: (Int, String?, String?, String) -> Unit

) {
    val stats = viewModel.sharedStats.collectAsState()
    val subtitle = viewModel.sharedSubtitle.collectAsState()
    val period = viewModel.sharedPeriodEnabled.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(type, (type as? StatsType.MapStats)?.periodic ?: period.value)

    }

    LaunchedEffect(Unit) {
        viewModel.sharedWarDetailsClick.collect {
            onWarDetailsClick(type, period.value == Periodics.Week.name)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sharedTrackDetailsClick.collect {
            val userId = (type as? StatsType.IndivStats)?.userId
            val teamId = (type as? StatsType.OpponentStats)?.teamId
            onTrackDetailsClick(userId, teamId, period.value)
        }
    }
    MKBaseScreen(title = type.title, subTitle = subtitle.value) {
        (type as? StatsType.MapStats)?.trackIndex?.let {
            MKTrackItem(map = Maps.values().getOrNull(it))
        }
        if (type is StatsType.IndivStats || type is StatsType.TeamStats) {
            MKSegmentedSelector(buttons = listOf(
                Pair(stringResource(id = R.string.all)) { viewModel.init(type, Periodics.All.name) },
                Pair(stringResource(id = R.string.hebdo)) { viewModel.init(type, Periodics.Week.name) },
                Pair(stringResource(id = R.string.mensuel)) { viewModel.init(type, Periodics.Month.name) },
            ), indexSelected = when (period.value) {
                Periodics.All.name -> 0
                Periodics.Week.name -> 1
                else -> 2
            })
        }
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (val mkStats = stats.value) {
                null ->  MKProgress()
                else -> {
                    MKWarStatsView(mkStats = mkStats)
                    type.takeIf { it is StatsType.IndivStats || (it as? StatsType.OpponentStats)?.userId != null }?.let {
                        (stats.value as? Stats) ?.let { MKPlayerScoreStatsView(stats = it, onHighestClick = goToWarDetails, onLowestClick = goToWarDetails) }
                    }
                    MKWarDetailsStatsView(mkStats = mkStats, type = type)
                    type.takeIf { it is StatsType.IndivStats || it is StatsType.TeamStats }?.let {
                        (stats.value as? Stats) ?.let {
                            MKTeamStatsView(stats = it, userId = (type as? StatsType.IndivStats)?.userId, onLessDefeatedClick = goToOpponentStats, onMostDefeatedClick = goToOpponentStats, onMostPlayedClick = goToOpponentStats)
                        }
                    }
                    MKTeamScoreStatView(stats = mkStats, onHighestClick = goToWarDetails, onLoudestClick = goToWarDetails)
                    type.takeIf { it !is StatsType.MapStats }?.let {
                        (stats.value as? Stats) ?.let { MKMapsStatsView(
                            stats = it,
                            type = type,
                            periodic = period.value,
                            onMostPlayedClick = goToMapStats,
                            onBestClick = goToMapStats,
                            onWorstClick = goToMapStats
                        ) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        MKButton(text = R.string.voir_le_d_tail) {
                            viewModel.onDetailsWarClick()
                        }
                        if (type !is StatsType.MapStats)
                            MKButton(text = R.string.voir_le_d_tail_map) {
                                viewModel.onDetailsTrackClick()
                            }
                    }
                }
            }
        }
    }
}