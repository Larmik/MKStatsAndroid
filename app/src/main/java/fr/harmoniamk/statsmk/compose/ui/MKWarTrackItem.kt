package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import fr.harmoniamk.statsmk.model.local.MapDetails

@Composable
fun MKWarTrackItem(details: MapDetails, isIndiv: Boolean) {
    Row {
        Column {
            MKText(text = details.war.name.orEmpty())
            MKText(text = details.war.war?.createdDate.orEmpty())
        }
        when (isIndiv) {
            true -> MKText(text = details.position.toString())
            else -> Column {
                MKText(text = details.warTrack.displayedResult)
                MKText(text = details.warTrack.displayedDiff)
            }
        }
    }
}