package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKPlayerList
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.CurrentWarViewModel

@Composable
fun CurrentWarScreen(viewModel: CurrentWarViewModel = hiltViewModel()) {

    val war = viewModel.sharedCurrentWar.collectAsState()
    val players = viewModel.sharedWarPlayers.collectAsState()
    val tracks = viewModel.sharedTracks.collectAsState()

    val buttons = listOf(
        Pair(R.string.remplacement, {}),
        Pair(R.string.p_nalit, {}),
        Pair(R.string.annuler_le_match, {}),
    )

    MKBaseScreen(title = war.value?.name ?: "", subTitle = war.value?.displayedState) {
        MKSegmentedButtons(buttons = buttons)
        players.value?.let {
            MKText(text = R.string.joueurs, font = R.font.montserrat_bold)
            MKPlayerList(players = it)
        }
        MKScoreView(war = war.value, modifier = Modifier.background(color = colorResource(id = R.color.transparent_white), shape = RoundedCornerShape(5.dp)))
        MKButton(text = R.string.prochaine_course) {

        }
        Spacer(modifier = Modifier.height(10.dp))
        tracks.value?.let {
            MKText(text = R.string.courses_jou_es, font = R.font.montserrat_bold)
            LazyColumn(Modifier.padding(10.dp)) {
                items(items = it) {
                    MKTrackItem(track = it)
                }
            }
        }
    }
}