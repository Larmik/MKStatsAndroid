package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.compose.ui.FAQCell
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.viewModel.FAQViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FAQScreen(viewModel: FAQViewModel = hiltViewModel()) {

    MKBaseScreen(title = "Aide & Contact") {
        LazyColumn {
            items(viewModel.faqCells) {
                FAQCell(it)
            }
        }
    }
}