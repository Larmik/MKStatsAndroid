package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKDialog
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.ResetPasswordViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun ResetPasswordScreen(viewModel: ResetPasswordViewModel = hiltViewModel(), onDismiss: () -> Unit) {

    val emailState = remember { mutableStateOf(TextFieldValue("")) }
    val dialogState = viewModel.sharedDialogValue.collectAsState()

    dialogState.value?.let {
        MKDialog(state = it)
    }

    LaunchedEffect(Unit) {
        viewModel.sharedDismiss.collect {
            onDismiss()
        }
    }

    MKBaseScreen(title = R.string.mot_de_passe_oubli) {
        MKTextField(
            value = emailState.value,
            placeHolderRes = R.string.entrez_votre_adresse_email,
            onValueChange = {
                emailState.value = it
            })
        MKButton(text = R.string.valider, enabled = emailState.value.text.isNotEmpty()) {
            viewModel.onReset(emailState.value.text)
        }
    }
}