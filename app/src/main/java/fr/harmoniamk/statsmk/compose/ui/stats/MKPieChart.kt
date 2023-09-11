package fr.harmoniamk.statsmk.compose.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.model.local.MKStats
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.model.local.Stats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlin.math.roundToInt

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun MKPieChart(
    modifier: Modifier = Modifier,
    stats: MKStats?
) {
    val warStats = (stats as? Stats)?.warStats
    val mapStats = stats as? MapStats

    val win = warStats?.warsWon ?: mapStats?.trackWon
    val tie = warStats?.warsTied ?: mapStats?.trackTie
    val loss = warStats?.warsLoss ?: mapStats?.trackLoss

    val values = listOf(win?.toFloat(), tie?.toFloat(), loss?.toFloat()).filterNotNull()
    val colors = listOf(
        colorResource(id = R.color.win),
        colorResource(id = R.color.white),
        colorResource(id = R.color.lose),
    )

    val sumOfValues = values.sum()
    val proportions = values.map { it * 100 / sumOfValues }
    val sweepAngles = proportions.map { 360 * it / 100 }
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .size(size = 170.dp)
        ) {
            var startAngle = -90f
            for (i in sweepAngles.indices) {
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = sweepAngles[i],
                    useCenter = true
                )
                startAngle += sweepAngles[i]
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.background(color = colorResource(id = R.color.harmonia_clear), shape = RoundedCornerShape(90.dp)).size(90.dp)) {
            MKText(text = "Winrate:")
            MKText(text = (((win?.toFloat() ?: 0f) * 100) / sumOfValues).roundToInt().toString() + " %", font = R.font.orbitron_regular, fontSize = 16)
        }
    }
}

@Preview
@Composable
fun MKPieChartPreview() {
    MKPieChart(
        stats = null
    )
}