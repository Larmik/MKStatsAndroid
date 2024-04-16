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

    val htmlTextColor = android.graphics.Color.parseColor("#${preferencesRepository.mainTextColor}")

    val mainColor = Color.fromHex("#${preferencesRepository.mainColor}")
    val secondaryColor = Color.fromHex("#${preferencesRepository.secondaryColor}")
    val mainTextColor = Color.fromHex("#${preferencesRepository.mainTextColor}")
    val secondaryTextColor = Color.fromHex("#${preferencesRepository.secondaryTextColor}")

    val secondaryColorTransparent = secondaryColor.copy(alpha = 0.5f)
    val secondaryColorAlphaed = secondaryColor.copy(alpha = 0.3f)
}