package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.fragment.settings.manageTeams.AddTeamViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun CreateTeamScreen(viewModel: AddTeamViewModel = hiltViewModel(), onDismiss: () -> Unit) {
    val nameState = remember { mutableStateOf(TextFieldValue("")) }
    val tagState = remember { mutableStateOf(TextFieldValue("")) }

    val enabled = nameState.value.text.isNotEmpty() && tagState.value.text.isNotEmpty()

    BackHandler { onDismiss() }
    LaunchedEffect(Unit) {
        viewModel.sharedTeamAdded.collect {
            onDismiss()
        }
    }
    MKBaseScreen(title = R.string.cr_er_une_quipe) {
        MKTextField(value = nameState.value, onValueChange = { nameState.value = it }, placeHolderRes = R.string.nom)
        MKTextField(value = tagState.value, onValueChange = { tagState.value = it }, placeHolderRes = R.string.tag)
        MKButton(text = R.string.enregistrer, enabled = enabled) {
            viewModel.onCreateClick(
                name = nameState.value.text,
                shortName = tagState.value.text,
                teamWithLeader = false
            )
        }
    }
}