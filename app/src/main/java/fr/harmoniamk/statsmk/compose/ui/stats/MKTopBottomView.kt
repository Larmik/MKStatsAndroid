package fr.harmoniamk.statsmk.compose.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.SpaceAround
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.extension.positionColor

@Composable
fun MKTopBottomView(indiv: Boolean, tops: List<Pair<String, Int>>?, bottoms:  List<Pair<String, Int>>?) {
    val padding = when (indiv) {
        true -> 60.dp
        else -> 30.dp
    }
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).padding(10.dp)) {
            MKText(text = "Tops", modifier = Modifier.padding(bottom = 10.dp), fontSize = 18, font = R.font.montserrat_bold)
            tops?.forEach {

                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = padding), horizontalArrangement = SpaceBetween, verticalAlignment = CenterVertically) {
                    when (indiv) {
                        true ->  MKText(text = it.first, font = R.font.mk_position, textColor = it.first.toIntOrNull().positionColor(), fontSize = 20)
                        else ->  MKText(text = it.first)
                    }
                    MKText(text = it.second.toString(), font = R.font.orbitron_semibold, fontSize = 16)
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).padding(10.dp) ) {
            MKText(text = "Bottoms", modifier = Modifier.padding(bottom = 10.dp), fontSize = 18, font = R.font.montserrat_bold)
            bottoms?.forEach {
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = padding), horizontalArrangement = SpaceBetween, verticalAlignment = CenterVertically) {
                    when (indiv) {
                        true ->  MKText(text = it.first, font = R.font.mk_position, textColor = it.first.toIntOrNull().positionColor(), fontSize = 20)
                        else ->  MKText(text = it.first)
                    }

                    MKText(text = it.second.toString(), font = R.font.orbitron_semibold, fontSize = 16)
                }
            }
        }
    }
}

@Preview
@Composable
fun MKTopBottomPreview() {
    MKTopBottomView(
        false,
        tops = listOf(
            Pair("Top 6", 5),
            Pair("Top 5", 5),
            Pair("Top 4", 5),
            Pair("Top 3", 5),
            Pair("Top 2", 5),
        ),
        bottoms =  listOf(
            Pair("Bottom 6", 5),
            Pair("Bottom 5", 5),
            Pair("Bottom 4", 5),
            Pair("Bottom 3", 5),
            Pair("Bottom 2", 5),
        )
    )
}
@Preview
@Composable
fun MKTopBottomPreviewIndiv() {
    MKTopBottomView(
        true,
        tops = listOf(
            Pair("1", 5),
            Pair("2", 5),
            Pair("3", 5),
            Pair("4", 5),
            Pair("5", 5),
            Pair("6", 5),
        ),
        bottoms =  listOf(
            Pair("7", 5),
            Pair("8", 5),
            Pair("9", 5),
            Pair("10", 5),
            Pair("11", 5),
            Pair("12", 5),
        )
    )
}