package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MKOpponentPerfCell() {
    Column() {
        MKText(text = "Epines volantes")
        MKText(text = "14 matchs joués ")
    }
}

@Preview
@Composable
fun MKOpponentPerfCellPreview() {
    MKOpponentPerfCell()
}