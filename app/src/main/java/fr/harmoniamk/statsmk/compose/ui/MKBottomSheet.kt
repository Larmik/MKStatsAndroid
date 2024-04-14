package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.screen.EditUserScreen
import fr.harmoniamk.statsmk.compose.screen.FilterSortScreen
import fr.harmoniamk.statsmk.compose.screen.PenaltyScreen
import fr.harmoniamk.statsmk.compose.screen.PlayersSettingsScreen
import fr.harmoniamk.statsmk.compose.screen.PositionScreen
import fr.harmoniamk.statsmk.compose.screen.ResetPasswordScreen
import fr.harmoniamk.statsmk.compose.screen.SubPlayerScreen
import fr.harmoniamk.statsmk.compose.screen.TrackListScreen
import fr.harmoniamk.statsmk.compose.screen.WarTrackResultScreen
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.compose.viewModel.Filter
import fr.harmoniamk.statsmk.compose.viewModel.Sort
import fr.harmoniamk.statsmk.enums.FilterType
import fr.harmoniamk.statsmk.enums.SortType
import fr.harmoniamk.statsmk.extension.fromHex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

sealed class MKBottomSheetState {
    class EditTrack : MKBottomSheetState()
    class EditPositions : MKBottomSheetState()
    class EditShocks : MKBottomSheetState()
    class SubPlayer : MKBottomSheetState()
    class Penalty : MKBottomSheetState()
    class EditUser : MKBottomSheetState()
    class ResetPassword : MKBottomSheetState()
    class StatsDisplayMode(val initialValue: Boolean): MKBottomSheetState()
    class FilterSort(val sort: Sort, val filter: Filter): MKBottomSheetState()
    class Theme(val initialMain: String, val initialSecondary: String): MKBottomSheetState()
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun MKBottomSheet(
    trackIndex: Int?,
    state: MKBottomSheetState?,
    onEditTrack: (Int) -> Unit = { },
    onDismiss: () -> Unit,
    onEditPosition: (Int) -> Unit = { },
    onSorted: (SortType) -> Unit = { },
    onFiltered: (List<FilterType>) -> Unit = { },
    onDisplayModeValidated: (Boolean) -> Unit = { },
    onColorsSelected: (String, String) -> Unit = { _,_ ->}
) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    when (state) {
        is MKBottomSheetState.EditTrack -> {
            trackIndex?.let {
                TrackListScreen(
                    trackIndex = it,
                    editing = true,
                    onDismiss = onDismiss,
                    onTrackClick = onEditTrack
                )
            }
        }
        is MKBottomSheetState.EditPositions -> {
            trackIndex?.let {
                PositionScreen(
                    trackIndex = it,
                    editing = true,
                    onBack = onDismiss,
                    onNext = onEditPosition
                )
            }
        }
        is MKBottomSheetState.EditShocks -> {
            trackIndex?.let {
                WarTrackResultScreen(
                    trackIndex = it,
                    editing = true,
                    onBack = onDismiss,
                    backToCurrent = onDismiss
                )
            }
        }
        is MKBottomSheetState.SubPlayer -> {
            SubPlayerScreen(onDismiss = onDismiss)
        }
        is MKBottomSheetState.Penalty -> {
            PenaltyScreen(onDismiss = onDismiss)
        }
        is MKBottomSheetState.EditUser -> {
            EditUserScreen(onDismiss = onDismiss)
        }
        is MKBottomSheetState.ResetPassword -> {
            ResetPasswordScreen(onDismiss = onDismiss)
        }
        is MKBottomSheetState.FilterSort -> {
            FilterSortScreen(sort = state.sort, filter = state.filter, onDismiss = onDismiss, onSorted = onSorted, onFiltered = onFiltered)
        }
        is MKBottomSheetState.StatsDisplayMode -> {
            val rosterOnly = remember { mutableStateOf(state.initialValue) }
            MKBaseScreen(title = "Calcul des statistiques", subTitle = "Option multi-roster") {
                MKText(modifier = Modifier.padding(20.dp), text = when (rosterOnly.value) {
                    true -> "Les statistiques sont calculées en fonction des wars de votre roster actuel seulement."
                    else -> "Les statistiques sont calculées en fonction des wars de tous les rosters de votre équipe."
                })
                Switch(checked = !rosterOnly.value, onCheckedChange = {
                    rosterOnly.value = !rosterOnly.value
                },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorsViewModel.secondaryColor,
                        checkedTrackColor = colorResource(R.color.harmonia_dark_alphaed)
                    ),
                )
                MKText(modifier = Modifier.padding(20.dp), text = when (rosterOnly.value) {
                    true -> "Activez le paramètre pour prendre en compte tous les rosters."
                    else -> "Désactivez le paramètre pour ne prendre en compte que votre roster."
                }, font = R.font.montserrat_bold)

                Row {
                    MKButton(text = "Valider") {
                        onDisplayModeValidated(rosterOnly.value)
                    }
                    MKButton(
                        text = "Retour",
                        onClick = onDismiss,
                        hasBackground = false
                    )
                }

            }
        }
        is MKBottomSheetState.Theme -> {
            val mainColor = remember { mutableStateOf(TextFieldValue(state.initialMain)) }
            val secondaryColor = remember { mutableStateOf(TextFieldValue(state.initialSecondary)) }
            val hexaRegex = Regex("[A-Fa-f0-9]{6}")
            MKBaseScreen(title = "Thème", subTitle = "Couleurs") {
                MKText(modifier = Modifier.padding(20.dp), text = stringResource(R.string.main_color))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MKText(text = "#", fontSize = 18, font = R.font.montserrat_bold)
                    MKTextField(
                        value = mainColor.value,
                        modifier = Modifier.fillMaxWidth(0.3f).padding(horizontal = 10.dp),
                        onValueChange = { mainColor.value = it },
                        placeHolderRes = R.string.hexa,
                    )
                    Box(Modifier.size(50.dp).background(colorResource(R.color.white), shape = RoundedCornerShape(5.dp))) {
                        mainColor.value.text.takeIf { it.matches(hexaRegex) }?.let {
                            Spacer(Modifier.size(42.dp).align(Alignment.Center).background(color = Color.fromHex("#$it"), shape = RoundedCornerShape(5.dp)))
                        }
                    }


                }
 MKText(modifier = Modifier.padding(20.dp), text = stringResource(R.string.secondary_color))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MKText(text = "#", fontSize = 18, font = R.font.montserrat_bold)
                    MKTextField(
                        modifier = Modifier.fillMaxWidth(0.3f).padding(horizontal = 10.dp),
                        value = secondaryColor.value,
                        onValueChange = { secondaryColor.value = it },
                        placeHolderRes = R.string.hexa,
                    )
                    Box(Modifier.size(50.dp).background(colorResource(R.color.white), shape = RoundedCornerShape(5.dp))) {
                        secondaryColor.value.text.takeIf { it.matches(hexaRegex) }?.let {
                            Spacer(Modifier.size(42.dp).align(Alignment.Center).background(color = Color.fromHex("#$it"), shape = RoundedCornerShape(5.dp)))
                        }
                    }
                }

                Row {
                    MKButton(text = "Valider", enabled = mainColor.value.text.matches(hexaRegex) && secondaryColor.value.text.matches(hexaRegex)) {
                        onColorsSelected(mainColor.value.text, secondaryColor.value.text)
                    }
                    MKButton(
                        text = "Retour",
                        onClick = onDismiss,
                        hasBackground = false
                    )
                }

            }
        }
        else -> {}
    }
}