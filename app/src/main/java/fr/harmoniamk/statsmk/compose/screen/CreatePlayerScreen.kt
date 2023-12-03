package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.CreatePlayerViewModel
import fr.harmoniamk.statsmk.repository.mock.DatabaseRepositoryMock
import fr.harmoniamk.statsmk.repository.mock.FirebaseRepositoryMock
import fr.harmoniamk.statsmk.repository.mock.PreferencesRepositoryMock
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreatePlayerScreen(viewModel: CreatePlayerViewModel = hiltViewModel(), onDismiss: () -> Unit) {
    val nameState = remember { mutableStateOf(TextFieldValue("")) }
    val addToTeam = remember { mutableStateOf(false) }
    val addAlly = remember { mutableStateOf(false) }

    BackHandler { onDismiss() }

    LaunchedEffect(Unit) {
        viewModel.sharedDismiss.collect {
            onDismiss()
        }
    }

    MKBaseScreen(title = R.string.cr_er_un_joueur) {
        MKTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            placeHolderRes = R.string.nom
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = addToTeam.value, onCheckedChange = { addToTeam.value = it })
            MKText(text = R.string.int_grer_ce_joueur_l_quipe)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = addAlly.value, onCheckedChange = { addAlly.value = it })
            MKText(text = R.string.ajouter_en_tant_qu_ally)
        }
        MKButton(text = R.string.valider, enabled = nameState.value.text.isNotEmpty()) {
            viewModel.onPlayerCreated(nameState.value.text, addToTeam.value, addAlly.value)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Preview
@Composable
fun CreatePlayerPreview() {
    CreatePlayerScreen(
        viewModel = CreatePlayerViewModel(
            firebaseRepository = FirebaseRepositoryMock(),
            databaseRepository = DatabaseRepositoryMock(),
            preferencesRepository = PreferencesRepositoryMock()
        )
    ) {

    }
}