package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKCurrentWarCell
import fr.harmoniamk.statsmk.compose.ui.MKLifecycleEvent
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKWarItem
import fr.harmoniamk.statsmk.compose.viewModel.WarViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun WarScreen(
    viewModel: WarViewModel = hiltViewModel(),
    onCurrentWarClick: () -> Unit,
    onWarClick: (String?) -> Unit,
    onCreateWarClick: () -> Unit
) {
    val currentWar = viewModel.sharedCurrentWar.collectAsState()
    val loadingState = viewModel.sharedCurrentWarState.collectAsState()
    val lastWars = viewModel.sharedLastWars.collectAsState()
    val team = viewModel.sharedTeam.collectAsState()
    val createWarVisible = viewModel.sharedCreateWarVisible.collectAsState()
    val dispos = viewModel.sharedDispos.collectAsState()
    val buttons = listOfNotNull(
        Pair(R.string.cr_er_une_war, onCreateWarClick).takeIf { createWarVisible.value },
        Pair(R.string.ajouter_les_dispos, {}).takeIf { !dispos.value.isNullOrEmpty() },
    )

    MKLifecycleEvent {
        if (it == Lifecycle.Event.ON_RESUME)
            viewModel.refresh()
    }

    MKBaseScreen(title = R.string.team_war, subTitle = team.value?.team_name) {
        when  {
            lastWars.value == null -> {
                Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 10.dp), color = colorResource(
                            id = R.color.harmonia_dark
                        )
                    )
                    MKText(text = "Récupération des derniers résultats...", fontSize = 12)
                }
            }
            else -> {
                MKSegmentedButtons(buttons = buttons)
                currentWar.value?.let {
                    MKText(modifier = Modifier.padding(top = 10.dp), text = R.string.war_en_cours, font = R.font.montserrat_bold)
                    MKCurrentWarCell(it, loadingState.value, onCurrentWarClick)
                }
                when (lastWars.value.isNullOrEmpty()) {
                    true -> MKText(
                        text = R.string.jouez_d_abord_des_wars_afin_de_les_voir_appara_tre_ici,
                        modifier = Modifier.padding(top = 10.dp),
                        font = R.font.montserrat_bold
                    )
                    else -> {
                        MKText(
                            text = R.string.derni_res_wars,
                            modifier = Modifier.padding(top = 10.dp),
                            font = R.font.montserrat_bold
                        )
                        LazyColumn(Modifier.padding(10.dp)) {
                            items(items = lastWars.value.orEmpty()) {
                                MKWarItem(war = it, onClick = onWarClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun WarScreenPreview() {
    WarScreen(onCurrentWarClick = {}, onWarClick = {}, onCreateWarClick = {})
}