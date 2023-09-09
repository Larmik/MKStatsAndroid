package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.EditUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun EditUserScreen(
    viewModel: EditUserViewModel = hiltViewModel(),
    emailEditing: Boolean,
    onDismiss: () -> Unit
) {
    val fieldState = remember { mutableStateOf(TextFieldValue("")) }
    val title = when (emailEditing) {
        true -> R.string.edit_mail
        else -> R.string.edit_nickname
    }

    BackHandler { onDismiss() }
    MKBaseScreen(title = title) {
        MKTextField(
            value = fieldState.value,
            onValueChange = { fieldState.value = it },
            placeHolderRes = R.string.modifier_le_nom
        )
        MKButton(text = R.string.enregistrer, enabled = fieldState.value.text.isNotEmpty()) {
            viewModel.onValidate(emailEditing, fieldState.value.text)
            onDismiss()
        }
    }
}