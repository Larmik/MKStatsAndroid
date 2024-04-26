package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.screen.EditUserScreen
import fr.harmoniamk.statsmk.compose.screen.FilterSortScreen
import fr.harmoniamk.statsmk.compose.screen.PenaltyScreen
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
    class StatsDisplayMode(val initialValue: Boolean) : MKBottomSheetState()
    class FilterSort(val sort: Sort, val filter: Filter) : MKBottomSheetState()
    class Theme(
        val initialMain: String,
        val initialSecondary: String,
        val initialMainText: String,
        val initialSecondaryText: String
    ) : MKBottomSheetState()
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
    onColorsSelected: (String, String, String, String) -> Unit = { _, _, _, _ -> }
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
            FilterSortScreen(
                sort = state.sort,
                filter = state.filter,
                onDismiss = onDismiss,
                onSorted = onSorted,
                onFiltered = onFiltered
            )
        }

        is MKBottomSheetState.StatsDisplayMode -> {
            val rosterOnly = remember { mutableStateOf(state.initialValue) }
            MKBaseScreen(title = stringResource(R.string.calcul_des_statistiques), subTitle = stringResource(
                R.string.option_multi_roster
            )
            ) {
                MKText(modifier = Modifier.padding(20.dp), text = when (rosterOnly.value) {
                        true -> stringResource(R.string.multi_roster_disabled_label)
                        else -> stringResource(R.string.multi_roster_enabled_label)
                    })
                Switch(
                    checked = !rosterOnly.value,
                    onCheckedChange = {
                        rosterOnly.value = !rosterOnly.value
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorsViewModel.secondaryColor,
                        checkedTrackColor = colorsViewModel.secondaryColorAlphaed
                    ),
                )
                MKText(
                    modifier = Modifier.padding(20.dp), text = when (rosterOnly.value) {
                        true -> stringResource(R.string.enable_multi_roster)
                        else -> stringResource(R.string.disable_multi_roster)
                    }, font = R.font.montserrat_bold
                )

                Row {
                    MKButton(text = stringResource(R.string.valider)) {
                        onDisplayModeValidated(rosterOnly.value)
                    }
                    MKButton(
                        text = stringResource(R.string.retour),
                        onClick = onDismiss,
                        hasBackground = false
                    )
                }

            }
        }

        is MKBottomSheetState.Theme -> {
            val mainColor = remember { mutableStateOf(TextFieldValue(state.initialMain)) }
            val secondaryColor = remember { mutableStateOf(TextFieldValue(state.initialSecondary)) }
            val mainTextColor = remember { mutableStateOf(state.initialMainText) }
            val secondaryTextColor = remember { mutableStateOf(state.initialSecondaryText) }
            val hexaRegex = Regex("[A-Fa-f0-9]{6}")
            MKBaseScreen(title = stringResource(R.string.th_me), subTitle = stringResource(R.string.couleurs)) {
                MKText(
                    modifier = Modifier.padding(20.dp),
                    text = stringResource(R.string.main_color),
                    font = R.font.montserrat_bold
                )

                MKText(text = "Fond")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MKText(text = "#", fontSize = 18, font = R.font.montserrat_bold)
                    MKTextField(
                        value = mainColor.value,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(horizontal = 10.dp),
                        onValueChange = { mainColor.value = it },
                        placeHolderRes = R.string.hexa,
                    )
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(
                                colorResource(R.color.white),
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        mainColor.value.text.takeIf { it.matches(hexaRegex) }?.let {
                            Spacer(
                                Modifier
                                    .size(32.dp)
                                    .align(Alignment.Center)
                                    .background(
                                        color = Color.fromHex("#$it"),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            )
                        }
                    }
                }


                MKText(text = stringResource(R.string.texte), modifier = Modifier.padding(top = 20.dp))
                MKSegmentedSelector(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(vertical = 5.dp, horizontal = 60.dp)
                        .border(width = 1.dp, color = colorsViewModel.secondaryColor),
                    buttons = listOf(
                        Pair(stringResource(R.string.noir), { mainTextColor.value = "000000" }),
                        Pair(stringResource(R.string.blanc), { mainTextColor.value = "FFFFFF" })
                    ), indexSelected = when (mainTextColor.value) {
                        "000000" -> 0
                        else -> 1
                    }
                )

                MKText(
                    modifier = Modifier.padding(20.dp),
                    text = stringResource(R.string.secondary_color),
                    font = R.font.montserrat_bold
                )
                MKText(text = stringResource(R.string.fond))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MKText(text = "#", fontSize = 20, font = R.font.montserrat_bold)
                    MKTextField(
                        value = secondaryColor.value,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(horizontal = 10.dp),
                        onValueChange = { secondaryColor.value = it },
                        placeHolderRes = R.string.hexa,
                    )
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(
                                colorResource(R.color.white),
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        secondaryColor.value.text.takeIf { it.matches(hexaRegex) }?.let {
                            Spacer(
                                Modifier
                                    .size(32.dp)
                                    .align(Alignment.Center)
                                    .background(
                                        color = Color.fromHex("#$it"),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            )
                        }
                    }
                }
                MKText(text = stringResource(R.string.texte), modifier = Modifier.padding(top = 20.dp))
                MKSegmentedSelector(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(vertical = 5.dp, horizontal = 60.dp)
                        .border(width = 1.dp, color = colorsViewModel.secondaryColor),
                    buttons = listOf(
                        Pair(stringResource(R.string.noir), { secondaryTextColor.value = "000000" }),
                        Pair(stringResource(R.string.blanc), { secondaryTextColor.value = "FFFFFF" })
                    ), indexSelected = when (secondaryTextColor.value) {
                        "000000" -> 0
                        else -> 1
                    }
                )

                Row(modifier = Modifier.padding(top = 20.dp)) {
                    MKButton(
                        text = stringResource(R.string.valider),
                        enabled = mainColor.value.text.matches(hexaRegex) && secondaryColor.value.text.matches(
                            hexaRegex
                        )
                    ) {
                        onColorsSelected(
                            mainColor.value.text,
                            secondaryColor.value.text,
                            mainTextColor.value,
                            secondaryTextColor.value
                        )
                    }
                    MKButton(
                        text = stringResource(R.string.retour),
                        onClick = onDismiss,
                        hasBackground = false
                    )
                }

            }
        }

        else -> {}
    }
}