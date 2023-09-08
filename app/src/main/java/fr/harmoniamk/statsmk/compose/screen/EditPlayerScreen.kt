package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKRadioGroup
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.fragment.settings.managePlayers.EditPlayerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun EditPlayerScreen(viewModel: EditPlayerViewModel = hiltViewModel(), playerId: String, onDismiss: () -> Unit) {
    viewModel.refresh(playerId)
    val playerHasAccount by viewModel.sharedPlayerHasAccount.collectAsState()
    val leaveTeamVisible = viewModel.sharedLeaveTeamVisibility.collectAsState()
    val player by viewModel.sharedPlayer.collectAsState()
    val nameState = remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        viewModel.sharedDismiss.collect {
            onDismiss()
        }
    }

    BackHandler { onDismiss() }
    MKBaseScreen(title = "Edition joueur", subTitle = player?.name.orEmpty()) {
        when (playerHasAccount) {
            true -> {
                MKRadioGroup(
                    defaultOption = UserRole.values().single { it.ordinal == player?.role },
                    options = UserRole.values().filterNot { it == UserRole.GOD },
                    onSelected = { player?.role = it }
                )

                MKButton(text = R.string.enregistrer) {
                    viewModel.onPlayerEdited(player?.name.orEmpty(), player?.role)
                }
            }
            else -> {
                MKTextField(value = nameState.value, onValueChange = { nameState.value = it }, placeHolderRes = R.string.modifier_le_nom)
                MKButton(text = R.string.enregistrer, enabled = nameState.value.text.isNotEmpty() ) {
                    viewModel.onPlayerEdited(nameState.value.text, player?.role)
                }
            }
        }

        if (leaveTeamVisible.value)
            MKButton(text = R.string.retirer_ce_joueur_de_l_quipe, hasBackground = false) {
                viewModel.onRemoveFromTeam(player)
            }

    }
}