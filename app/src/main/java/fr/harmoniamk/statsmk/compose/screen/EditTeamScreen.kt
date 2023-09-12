package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.EditTeamViewModel
import fr.harmoniamk.statsmk.repository.mock.AuthenticationRepositoryMock
import fr.harmoniamk.statsmk.repository.mock.DatabaseRepositoryMock
import fr.harmoniamk.statsmk.repository.mock.FirebaseRepositoryMock
import fr.harmoniamk.statsmk.repository.mock.PreferencesRepositoryMock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun EditTeamScreen(
    viewModel: EditTeamViewModel = hiltViewModel(),
    teamId: String,
    onDismiss: () -> Unit
) {
    viewModel.refresh(teamId)

    val team = viewModel.sharedTeam.collectAsState()
    val nameState = remember { mutableStateOf(TextFieldValue("")) }
    val tagState = remember { mutableStateOf(TextFieldValue("")) }
    val enabled = nameState.value.text.isNotEmpty() && tagState.value.text.isNotEmpty()
            && (nameState.value.text.lowercase() != team.value?.name?.lowercase()
            || tagState.value.text.lowercase() != team.value?.shortName?.lowercase())

    BackHandler { onDismiss() }
    LaunchedEffect(Unit) {
        viewModel.sharedDismiss.collect {
            onDismiss()
        }
    }
    MKBaseScreen(title = "Edition équipe", subTitle = team.value?.name.orEmpty()) {
        MKTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            placeHolderRes = R.string.modifier_le_nom,
            keyboardType = KeyboardType.Text
        )
        MKTextField(
            value = tagState.value,
            onValueChange = { tagState.value = it },
            placeHolderRes = R.string.modifier_le_tag,
            keyboardType = KeyboardType.Text
        )
        MKButton(
            text = R.string.enregistrer,
            enabled = enabled
        ) { viewModel.onTeamEdited(nameState.value.text, tagState.value.text) }
    }
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Preview
@Composable
fun EditTeamPreview() {
    EditTeamScreen(
        viewModel = EditTeamViewModel(
            firebaseRepository = FirebaseRepositoryMock(),
            databaseRepository = DatabaseRepositoryMock(),
            preferencesRepository = PreferencesRepositoryMock(),
            authenticationRepository = AuthenticationRepositoryMock()
        ), teamId = "12345"
    ) {

    }
}