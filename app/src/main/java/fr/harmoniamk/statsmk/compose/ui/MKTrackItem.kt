package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R

@Composable
fun MKTrackItem(isVertical: Boolean = false) {
    Card() {
        when (isVertical) {
            true -> {
                Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = R.drawable.bc), contentDescription = null, modifier = Modifier
                        .width(100.dp)
                        .height(60.dp))
                    MKText(text = "Château de Bowser", font = R.font.montserrat_bold)
                    MKText(text = "BC")

                }
            }
            else -> {
                Row(
                    Modifier
                        .padding(10.dp)
                        .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.bc), contentDescription = null, modifier = Modifier
                            .width(100.dp)
                            .height(60.dp)
                            .padding(end = 10.dp))
                        Column() {
                            MKText(text = "Château de Bowser", font = R.font.montserrat_bold)
                            MKText(text = "BC")
                        }
                    }

                    Image(painter = painterResource(id = R.drawable.special), contentDescription = null, modifier = Modifier.size(45.dp))
                }
            }
        }
    }
    
}

@Preview
@Composable
fun MKTrackItemPreview() {
    MKTrackItem()
}

@Preview
@Composable
fun MKTrackItemPreviewVertical() {
    MKTrackItem(true)
}