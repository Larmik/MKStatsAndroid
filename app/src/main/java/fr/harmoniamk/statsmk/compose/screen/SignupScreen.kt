package fr.harmoniamk.statsmk.compose.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKDialog
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.SignupViewModel
import kotlinx.coroutines.flow.filterNotNull

@ExperimentalMaterialApi
@Composable
fun SignupScreen(
    viewModel: SignupViewModel = hiltViewModel(),
    onLogin: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val emailValue = remember { mutableStateOf(TextFieldValue("")) }
    val passwordValue = remember { mutableStateOf(TextFieldValue("")) }
    val nicknameValue = remember { mutableStateOf(TextFieldValue("")) }
    val fcValue = remember { mutableStateOf(TextFieldValue("")) }
    val loadingState = viewModel.sharedDialogValue.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sharedNext.filterNotNull().collect {
            onNext()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sharedToast.filterNotNull().collect {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    BackHandler { onBack() }
    loadingState.value?.let {
        MKDialog(state = it)
    }
    MKBaseScreen(
        title = stringResource(id = R.string.bienvenue),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 15.dp)
        ) {
            MKTextField(
                value = nicknameValue.value,
                onValueChange = { nicknameValue.value = it },
                placeHolderRes = R.string.entrez_votre_pseudo
            )
            MKTextField(
                value = emailValue.value,
                onValueChange = { emailValue.value = it },
                placeHolderRes = R.string.entrez_votre_adresse_email,
                keyboardType = KeyboardType.Email
            )
            MKTextField(
                value = passwordValue.value,
                onValueChange = { passwordValue.value = it },
                placeHolderRes = R.string.entrez_votre_mot_de_passe,
                keyboardType = KeyboardType.Password
            )
            MKTextField(
                value = fcValue.value,
                onValueChange = { fcValue.value = it },
                placeHolderRes = R.string.code_ami,
                keyboardType = KeyboardType.Number
            )
            MKButton(
                text = R.string.suivant,
                enabled = emailValue.value.text.isNotEmpty() && passwordValue.value.text.isNotEmpty() && nicknameValue.value.text.isNotEmpty()
            ) {
                viewModel.onSignup(
                    emailValue.value.text,
                    passwordValue.value.text,
                    nicknameValue.value.text,
                    fcValue.value.text
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MKButton(text = R.string.d_j_sur_l_appli, hasBackground = false, onClick = onLogin)
        }
    }
}
