package fr.harmoniamk.statsmk.compose.viewModel

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ViewModelFactoryProvider {
    val warDetailsViewModel: WarDetailsViewModel.Factory
    val positionViewModel: PositionViewModel.Factory
}