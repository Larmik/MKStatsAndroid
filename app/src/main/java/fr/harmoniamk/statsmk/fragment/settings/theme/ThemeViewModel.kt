package fr.harmoniamk.statsmk.fragment.settings.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
@FlowPreview
@ExperimentalCoroutinesApi
class ThemeViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _onThemeSelected = MutableSharedFlow<Unit>()
    val onThemeSelected = _onThemeSelected.asSharedFlow()

    fun bind(onMarioClick : Flow<Unit>, onLuigiClick: Flow<Unit>, onWarioClick: Flow<Unit>, onWaluigiClick: Flow<Unit>) {

        onMarioClick
            .onEach { preferencesRepository.currentTheme = R.style.AppThemeMario }
            .bind(_onThemeSelected, viewModelScope)
        onLuigiClick
            .onEach { preferencesRepository.currentTheme = R.style.AppThemeLuigi }
            .bind(_onThemeSelected, viewModelScope)
        onWaluigiClick
            .onEach { preferencesRepository.currentTheme = R.style.AppThemeWaluigi }
            .bind(_onThemeSelected, viewModelScope)
        onWarioClick
            .onEach { preferencesRepository.currentTheme = R.style.AppThemeWario }
            .bind(_onThemeSelected, viewModelScope)


    }

}