package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.compose.viewModel.SubPlayerViewModel
import fr.harmoniamk.statsmk.extension.isTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SubPlayerScreen(viewModel: SubPlayerViewModel = hiltViewModel(), onDismiss: () -> Unit) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    val currentPlayers = viewModel.sharedPlayers.collectAsState()
    val allies = viewModel.sharedAllies.collectAsState()
    val playerSelected = viewModel.sharedPlayerSelected.collectAsState()
    val title = viewModel.sharedTitle.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }

    BackHandler { viewModel.onBack() }
    LaunchedEffect(Unit) {
        viewModel.sharedBack.collect {
            onDismiss()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.refresh()

    }
    MKBaseScreen(title = title.value) {
        playerSelected.value?.let {
            MKTextField(value = searchState.value, onValueChange = {
                searchState.value = it
                viewModel.onSearch(it.text)
            }, placeHolderRes = R.string.rechercher_un_joueur)
        }
        LazyColumn(Modifier.weight(1f)) {
            if (allies.value.isNotEmpty())
                stickyHeader {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(40.dp).background(color = colorsViewModel.secondaryColor)) {
                        MKText(font = R.font.montserrat_bold, fontSize = 18, text = "Roster", textColor = R.color.white)
                    }
                }
            items(currentPlayers.value) {
                MKPlayerItem(
                    player = it.user,
                    isSelected = it.isSelected.isTrue,
                    onAddShock = {},
                    onRemoveShock = {},
                    onRootClick = {
                        it.user?.let { user ->
                            when (playerSelected.value) {
                                null -> viewModel.onOldPlayerSelect(user)
                                else -> viewModel.onNewPlayerSelect(user)
                            }
                        }
                    }
                )
            }
            allies.value.takeIf { it.isNotEmpty() }?.let {
                stickyHeader {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(40.dp).background(color = colorsViewModel.secondaryColor)) {
                        MKText(font = R.font.montserrat_bold, fontSize = 18, text = "Allies", textColor = R.color.white)
                    }
                }
                items(it) {
                    MKPlayerItem(
                        player = it.user,
                        isSelected = it.isSelected.isTrue,
                        onAddShock = {},
                        onRemoveShock = {},
                        onRootClick = {
                            it.user?.let { user ->
                                when (playerSelected.value) {
                                    null -> viewModel.onOldPlayerSelect(user)
                                    else -> viewModel.onNewPlayerSelect(user)
                                }
                            }
                        }
                    )
                }
            }
        }
        playerSelected.value?.let {
            MKButton(
                text = String.format(stringResource(id = R.string.sub_confirm), it.name),
                enabled = currentPlayers.value.any { it.isSelected.isTrue },
                onClick = viewModel::onSubClick
            )
        }
    }
}