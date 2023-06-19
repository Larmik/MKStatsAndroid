package fr.harmoniamk.statsmk.compose.ui

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
import kotlin.math.roundToInt

@Composable
fun MKPieChart(
    win: Float,
    tie: Float,
    lose: Float
) {
    val values = listOf(win, tie, lose)
    val colors = listOf(
        colorResource(id = R.color.win),
        colorResource(id = R.color.white),
        colorResource(id = R.color.lose),
    )

    val sumOfValues = values.sum()
    val proportions = values.map { it * 100 / sumOfValues }
    val sweepAngles = proportions.map { 360 * it / 100 }
    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(size = 200.dp)
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
            MKText(text = ((win * 100) / sumOfValues).roundToInt().toString() + " %", font = R.font.orbitron_regular, fontSize = 18)
        }
    }
}

@Preview
@Composable
fun MKPieChartPreview() {
    MKPieChart(
        win = 205f,
        tie = 13f,
        lose = 97f
    )
}