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
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel

@Composable
fun MKSegmentedButtons(modifier: Modifier = Modifier, buttons: List<Pair<String, () -> Unit>>, height: Int = 50){
    val colorsViewModel: ColorsViewModel = hiltViewModel()

    if (buttons.isNotEmpty())
        Row(
            modifier
                .fillMaxWidth()
                .height(height.dp)
                .padding(10.dp)
                .background(color = colorsViewModel.secondaryColor.copy(alpha = 0.6f), shape = RoundedCornerShape(5.dp)), verticalAlignment = Alignment.CenterVertically) {
            buttons.forEach {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { it.second() }
                    ) {
                    MKText(text = it.first, modifier = Modifier.fillMaxWidth(), font = R.font.roboto, newTextColor = colorsViewModel.secondaryTextColor)
                }
                if (buttons.indexOf(it) < buttons.size - 1)
                    Spacer(
                        Modifier
                            .width(1.dp)
                            .background(color = colorsViewModel.secondaryColor)
                            .fillMaxHeight())
            }
        }
}

