package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedSelector
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.PenaltyViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun PenaltyScreen(viewModel: PenaltyViewModel = hiltViewModel(), onDismiss: () -> Unit) {
    val amountValue = remember { mutableStateOf(TextFieldValue("")) }

    val team1 = viewModel.sharedTeam1.collectAsState()
    val team2 = viewModel.sharedTeam2.collectAsState()

    BackHandler { onDismiss() }

    LaunchedEffect(Unit) {
        viewModel.sharedDismiss.collect {
            onDismiss()
        }
    }

    MKBaseScreen(title = R.string.p_nalit) {
        MKSegmentedSelector(buttons = listOf(
            Pair(team1.value?.name.orEmpty()) { viewModel.onSelectTeam(team1.value?.mid) },
            Pair(team2.value?.name.orEmpty()) { viewModel.onSelectTeam(team2.value?.mid) }
        ))
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
        MKButton(
            text = R.string.infliger_la_p_nalit,
            enabled = amountValue.value.text.toIntOrNull() != null
        ) {
            viewModel.onPenaltyAdded()
        }
    }

}