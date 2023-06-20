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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKDialog
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.LoginViewModel
import kotlinx.coroutines.flow.filterNotNull

@ExperimentalMaterialApi
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel(), onNext: () -> Unit, onSignup: () -> Unit, onBack: () -> Unit) {

    val emailValue = remember { mutableStateOf(TextFieldValue("")) }
    val loadingState = viewModel.sharedLoading.collectAsState()
    val passwordValue = remember { mutableStateOf(TextFieldValue("")) }
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
        MKDialog(text = it, isLoading = true)
    }

    MKBaseScreen(title = stringResource(id = R.string.connexion), verticalArrangement = Arrangement.SpaceBetween) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 15.dp)) {
            MKTextField(value = emailValue.value, onValueChange = { emailValue.value = it }, placeHolderRes = R.string.entrez_votre_adresse_email)
            MKTextField(value = passwordValue.value, onValueChange = { passwordValue.value = it }, placeHolderRes = R.string.entrez_votre_mot_de_passe)
            MKButton(text = R.string.se_connecter, enabled = emailValue.value.text.isNotEmpty() && passwordValue.value.text.isNotEmpty()) {
                viewModel.onConnect(emailValue.value.text, passwordValue.value.text)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MKButton(text = R.string.mot_de_passe_oubli, hasBackground = false) {}
            MKButton(text = R.string.nouveau_sur_l_appli, hasBackground = false, onClick = onSignup)
        }
    }

}
