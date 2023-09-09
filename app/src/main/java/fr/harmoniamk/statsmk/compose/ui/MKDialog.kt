package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.isTrue

sealed class MKDialogState(
    val text: Any? = null,
    val isLoading: Boolean? = null,
    val positiveButtonText: Int? = null,
    val positiveButtonClick: () -> Unit = { },
    val negativeButtonText: Int = R.string.retour,
    val negativeButtonClick: () -> Unit = { }
) {
    class LeaveTeam(onTeamLeft: () -> Unit, onDismiss: () -> Unit) : MKDialogState(
        text = R.string.leave_team_confirm,
        positiveButtonText = R.string.quitter_mon_quipe,
        positiveButtonClick = onTeamLeft,
        negativeButtonClick = onDismiss
    )

    class ChangePassword(text: Any, onDismiss: () -> Unit) : MKDialogState(
        text = text,
        negativeButtonClick = onDismiss
    )

    class CancelWar(onWarCancelled: () -> Unit, onDismiss: () -> Unit) : MKDialogState(
        text = R.string.delete_war_confirm,
        positiveButtonText = R.string.supprimer_la_war,
        positiveButtonClick = onWarCancelled,
        negativeButtonClick = onDismiss
    )

    class Logout(onLogout: () -> Unit, onDismiss: () -> Unit) : MKDialogState(
        text = R.string.logout_confirm,
        positiveButtonText = R.string.se_d_connecter,
        positiveButtonClick = onLogout,
        negativeButtonClick = onDismiss
    )

    class Loading(loadingText: Int) : MKDialogState(
        text = loadingText,
        isLoading = true
    )
}

@Composable
fun MKDialog(state: MKDialogState) {
    Dialog(onDismissRequest = { }) {
        Column(
            Modifier
                .background(
                    color = colorResource(id = R.color.white),
                    shape = RoundedCornerShape(5.dp)
                )
                .padding(vertical = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            state.text?.let {
                MKText(
                    text = when (it) {
                        is String -> it
                        is Int -> stringResource(id = it)
                        else -> it.toString()
                    }, modifier = Modifier.padding(20.dp)
                )
            }
            state.isLoading?.takeIf { it }?.let {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 10.dp), color = colorResource(
                        id = R.color.harmonia_dark
                    )
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                state.positiveButtonText?.let {
                    MKButton(
                        text = it,
                        onClick = state.positiveButtonClick
                    )
                }
                state.takeIf { !it.isLoading.isTrue }?.let {
                    MKButton(
                        text = state.negativeButtonText,
                        onClick = state.negativeButtonClick,
                        hasBackground = false
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MKDialogPreviewWitButtons() {
    MKDialog(state = MKDialogState.LeaveTeam({}, {}))
}