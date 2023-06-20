package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.positionColor

@Composable
fun MKPositionSelector(position: Int, modifier: Modifier = Modifier, isVisible: Boolean = true, onClick: (Int) -> Unit) {
    Card(backgroundColor = colorResource(id = R.color.white_alphaed), modifier = modifier.padding(5.dp).clickable { onClick(position) }.alpha(if (isVisible) 1f else 0f)) {
        MKText(text = position.toString(), textColor = position.positionColor(), fontSize = 90, font = R.font.mk_position, modifier = Modifier.padding(10.dp))
    }
}

@Preview
@Composable
fun MKPositionPreview() {
    Column() {
        Row() {
            MKPositionSelector(position = 1, modifier = Modifier.weight(1f)) {}
            MKPositionSelector(position = 2, modifier = Modifier.weight(1f), isVisible = false) {}
            MKPositionSelector(position = 3, modifier = Modifier.weight(1f)) {}
        }
        Row() {
            MKPositionSelector(position = 4, modifier = Modifier.weight(1f)) {}
            MKPositionSelector(position = 5, modifier = Modifier.weight(1f)) {}
            MKPositionSelector(position = 6, modifier = Modifier.weight(1f)) {}
        }
        Row() {
            MKPositionSelector(position = 7, modifier = Modifier.weight(1f)) {}
            MKPositionSelector(position = 8, modifier = Modifier.weight(1f)) {}
            MKPositionSelector(position = 9, modifier = Modifier.weight(1f)) {}
        }
        Row() {
            MKPositionSelector(position = 10, modifier = Modifier.weight(1f)) {}
            MKPositionSelector(position = 11, modifier = Modifier.weight(1f)) {}
            MKPositionSelector(position = 12, modifier = Modifier.weight(1f)) {}
        }
    }
}