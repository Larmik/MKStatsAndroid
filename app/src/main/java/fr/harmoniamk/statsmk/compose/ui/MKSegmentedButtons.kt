package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R

@Composable
fun MKSegmentedButtons(buttons: List<Pair<Int, () -> Unit>>) {
    if (buttons.isNotEmpty())
        Row(
            Modifier
                .fillMaxWidth()
                .height(45.dp)
                .padding(bottom = 15.dp)
                .background(color = colorResource(id = R.color.transparent_white), shape = RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp)), verticalAlignment = Alignment.CenterVertically) {
            buttons.forEach {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { it.second() }
                    ) {
                    MKText(text = it.first, modifier = Modifier.fillMaxWidth(), font = R.font.roboto)
                }
                if (buttons.indexOf(it) < buttons.size - 1)
                    Spacer(
                        Modifier
                            .width(1.dp)
                            .background(color = colorResource(id = R.color.harmonia_dark))
                            .fillMaxHeight())
            }
        }
}

@Preview
@Composable
fun MKSegmentedButtonsPreview() {
    MKSegmentedButtons(listOf(
        Pair(R.string.cr_er_une_war, {}),
        Pair(R.string.le_coin_dispos, {}),
    ))
}