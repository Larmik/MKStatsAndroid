package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.fromHex
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ColorsViewModel @Inject constructor(preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {
    val mainColor = Color.fromHex("#${preferencesRepository.mainColor}")
    val secondaryColor = Color.fromHex("#${preferencesRepository.secondaryColor}")
}