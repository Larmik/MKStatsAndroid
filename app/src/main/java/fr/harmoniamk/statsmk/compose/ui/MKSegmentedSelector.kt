package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel

@Composable
fun MKSegmentedSelector(buttons: List<Pair<String, () -> Unit>>, indexSelected: Int = 0) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    val selectedIndex = remember { mutableStateOf(indexSelected) }
    Row(Modifier.fillMaxWidth()) {
        buttons.forEachIndexed { index, button ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .background(
                        color =  when (selectedIndex.value == index) {
                                true -> colorsViewModel.secondaryColorTransparent
                                else ->colorResource(R.color.transparent)
                            }

                    )
                    .clickable {
                        selectedIndex.value = index
                        button.second()
                    }) {
                val textColor =  when (selectedIndex.value == index) {
                    true -> colorsViewModel.secondaryTextColor
                    else -> colorsViewModel.mainTextColor
                }
                MKText(
                    text = button.first,
                    font = R.font.roboto,
                    newTextColor = textColor
                )
            }
        }
    }
}