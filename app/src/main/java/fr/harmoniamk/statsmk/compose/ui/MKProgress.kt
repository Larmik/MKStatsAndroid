package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel

@Composable
fun MKProgress() {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    CircularProgressIndicator(
        modifier = Modifier.padding(vertical = 10.dp), color = colorsViewModel.secondaryColor
    )
}