package fr.harmoniamk.statsmk.compose.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKWarItem
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.local.MKStats
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.model.local.Stats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun MKTeamScoreStatView(
    stats: MKStats,
    onHighestClick: (String?) -> Unit,
    onLoudestClick: (String?) -> Unit)
{
    val colorsviewModel: ColorsViewModel = hiltViewModel()
    val warVictory = (stats as? Stats)?.warStats?.highestVictory
    val warDefeat = (stats as? Stats)?.warStats?.loudestDefeat
    val tops = (stats as? MapStats)?.topsTable
    val bottoms = (stats as? MapStats)?.bottomsTable
    val indivTops = (stats as? MapStats)?.indivTopsTable
    val indivBottoms = (stats as? MapStats)?.indivBottomsTable

    Row(
        Modifier
            .padding(bottom = 20.dp)
            .border(1.dp, colorResource(id = R.color.black), RoundedCornerShape(5.dp))
            .background(
                color = colorsviewModel.secondaryColorTransparent,
                shape = RoundedCornerShape(5.dp)
            )) {
        warVictory?.let { war ->
            Column(
                Modifier
                    .weight(1f)
                    .padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.plus_large_victoire, fontSize = 12)
                MKWarItem(war = war, onClick = onHighestClick, isForStats = true)
            }
        }
        warDefeat?.let {
            Column(
                Modifier
                    .weight(1f)
                    .padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.plus_lourde_d_faite, fontSize = 12)
                MKWarItem(war = it, onClick = onLoudestClick, isForStats = true)
            }
        }
        if (tops.takeIf { it?.any { it.second > 0 }.isTrue } != null && bottoms.takeIf { it?.any { it.second > 0 }.isTrue } != null) {
            MKTopBottomView(false, tops, bottoms)
        }
        if (indivTops.takeIf { it?.any { it.second > 0 }.isTrue } != null && indivTops.takeIf { it?.any { it.second > 0 }.isTrue } != null) {
            MKTopBottomView(true, indivTops, indivBottoms)
        }

    }
}