package fr.harmoniamk.statsmk.compose.ui

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.model.local.MKWarTrack

@Composable
fun MKTrackItem(isVertical: Boolean = false, track: MKWarTrack? = null, map: Maps? = null) {

    Card(elevation = 0.dp, backgroundColor = colorResource(track?.backgroundColor ?: R.color.white_alphaed), modifier = Modifier.padding(bottom = 5.dp)) {
        when {
            track?.index != null -> Maps.values()[track.index ?: 0]
            map != null -> map
            else -> null
        }?.let { finalMap ->
            when (isVertical) {
                true -> {
                    Column(
                        Modifier
                            .padding(10.dp)
                            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = finalMap.picture), contentDescription = null, modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .width(100.dp)
                            .height(60.dp))
                        MKText(text = stringResource(id = finalMap.label), font = R.font.montserrat_bold, maxLines = 1)
                        MKText(text = finalMap.name, fontSize = 12)
                        Spacer(modifier = Modifier.height(5.dp))
                        track?.let {
                            MKScoreView(track = it, isSmaller = true, modifier = Modifier.fillMaxWidth().background(color = colorResource(R.color.white_alphaed), shape = RoundedCornerShape(5.dp)))
                        }
                    }
                }
                else -> {
                    Row(
                        Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(painter = painterResource(id = finalMap.picture), contentDescription = null, modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .width(100.dp)
                                .height(60.dp)
                                .padding(end = 10.dp))
                            Column() {
                                MKText(text = stringResource(id = finalMap.label), font = R.font.montserrat_bold)
                                MKText(text = finalMap.name)
                            }
                        }
                        when {
                            track != null -> MKScoreView(track = track, isSmaller = true)
                            map != null ->  Image(painter = painterResource(id = map.cup.picture), contentDescription = null, modifier = Modifier.size(45.dp))
                        }


                    }
                }
            }
        }

    }
    
}

@Preview
@Composable
fun MKTrackItemPreviewNormal() {
    MKTrackItem(map = Maps.dHC)
}

@Preview
@Composable
fun MKTrackItemPreviewVertical() {
    MKTrackItem(true)
}