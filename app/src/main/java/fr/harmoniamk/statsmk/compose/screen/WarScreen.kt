package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKCurrentWarCell
import fr.harmoniamk.statsmk.compose.ui.MKProgress
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKWarItem
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.compose.viewModel.WarViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun WarScreen(
    viewModel: WarViewModel = hiltViewModel(),
    onCurrentWarClick: (String) -> Unit,
    onWarClick: (String?) -> Unit,
    onCreateWarClick: () -> Unit
) {
    val colorsViewModel : ColorsViewModel = hiltViewModel()
    val currentWars = viewModel.sharedCurrentWars.collectAsState()
    val isLoading = viewModel.sharedLoading.collectAsState()
    val lastWars = viewModel.sharedLastWars.collectAsState()
    val team = viewModel.sharedTeam.collectAsState()
    val createWarVisible = viewModel.sharedCreateWarVisible.collectAsState()
    val dispos = viewModel.sharedDispos.collectAsState()
    val buttons = listOfNotNull(
        Pair(R.string.cr_er_une_war, onCreateWarClick).takeIf { createWarVisible.value },
        Pair(R.string.ajouter_les_dispos, {}).takeIf { !dispos.value.isNullOrEmpty() },
    )

    MKBaseScreen(title = R.string.team_war, subTitle = team.value?.team_name) {
        when  {
            isLoading.value -> {
                Column(Modifier.fillMaxWidth().padding(vertical = 10.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                   MKProgress()
                    MKText(text = "Récupération des derniers résultats...", fontSize = 12)
                }
            }
            else -> {
                MKSegmentedButtons(buttons = buttons)
                when (currentWars.value.isEmpty()) {
                    true -> {}
                    else -> {
                        MKText(modifier = Modifier.padding(top = 10.dp), fontSize = 16, text = R.string.war_en_cours, font = R.font.montserrat_bold)
                        currentWars.value.forEach { MKCurrentWarCell(it, onCurrentWarClick) }
                    }
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
                            fontSize = 16,
                            modifier = Modifier.padding(top = 10.dp),
                            font = R.font.montserrat_bold
                        )
                        LazyColumn(Modifier.padding(10.dp).padding(bottom = 60.dp)) {
                            lastWars.value?.forEach { (teamName, wars) ->
                                item {
                                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(color = colorResource(R.color.transparent))) {
                                        MKText(text = teamName, newTextColor = colorsViewModel.mainTextColor)
                                    }
                                }
                                items(items = wars) {
                                    MKWarItem(war = it, onClick = onWarClick)
                                }
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