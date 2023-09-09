package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.fragment.addPenalty.AddPenaltyViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun PenaltyScreen(viewModel: AddPenaltyViewModel = hiltViewModel(), onDismiss: () -> Unit) {
    val amountValue = remember { mutableStateOf(TextFieldValue("")) }

    val team1 = viewModel.sharedTeam1.collectAsState()
    val team2 = viewModel.sharedTeam2.collectAsState()
    val team1Selected = viewModel.sharedTeam1Selected.collectAsState()

    BackHandler { onDismiss() }

    MKBaseScreen(title = R.string.p_nalit) {
        Row(Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .background(
                        color = colorResource(
                            id = when (team1Selected.value) {
                                true -> R.color.transparent_white
                                else -> R.color.transparent
                            }
                        )
                    )
                    .clickable { viewModel.onSelectTeam(team1.value?.mid) }) {
                        MKText(
                            text = team1.value?.name.orEmpty(),
                            fontSize = 16
                        )
                    }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .background(
                        color = colorResource(
                            id = when (team1Selected.value) {
                                true -> R.color.transparent
                                else -> R.color.transparent_white
                            }
                        )
                    )
                    .clickable { viewModel.onSelectTeam(team2.value?.mid) }) {
                        MKText(
                            text = team2.value?.name.orEmpty(),
                            fontSize = 16
                        )
                    }
        }
        Spacer(modifier = Modifier.height(20.dp))
        MKTextField(
            modifier = Modifier.width(100.dp),
            value = amountValue.value,
            placeHolderRes = R.string.valeur,
            keyboardType = KeyboardType.Number,
            onValueChange = {
                amountValue.value = it
                viewModel.onAmount(it.text)
            })
        MKButton(text = R.string.infliger_la_p_nalit, enabled = amountValue.value.text.toIntOrNull() != null) {
            viewModel.onPenaltyAdded()
        }
    }

}