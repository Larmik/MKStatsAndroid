package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.local.MKWarTrack

@Composable
fun MKShockView(modifier: Modifier = Modifier, tracks: List<MKWarTrack>?) {
        var total = 0
        tracks?.forEach { track ->
            track.track?.shocks?.forEach {
                total += it.count
            }
        }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = modifier) {
                if (total > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.shock),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                        MKText(
                            text = String.format(
                                stringResource(id = R.string.shock_count_placeholder),
                                total.toString()
                            ),
                            font = R.font.orbitron_semibold,
                            fontSize = 16
                        )
                    }
                }
            }




}