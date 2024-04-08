package fr.harmoniamk.statsmk.compose

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.compose.viewModel.CurrentWarViewModel
import fr.harmoniamk.statsmk.compose.viewModel.PlayerListViewModel
import fr.harmoniamk.statsmk.compose.viewModel.PlayerProfileViewModel
import fr.harmoniamk.statsmk.compose.viewModel.PositionViewModel
import fr.harmoniamk.statsmk.compose.viewModel.StatsRankingViewModel
import fr.harmoniamk.statsmk.compose.viewModel.TeamProfileViewModel
import fr.harmoniamk.statsmk.compose.viewModel.TrackDetailsViewModel
import fr.harmoniamk.statsmk.compose.viewModel.TrackListViewModel
import fr.harmoniamk.statsmk.compose.viewModel.WarDetailsViewModel
import fr.harmoniamk.statsmk.compose.viewModel.WarTrackResultViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ViewModelFactoryProvider {
    val warDetailsViewModel: WarDetailsViewModel.Factory
    val positionViewModel: PositionViewModel.Factory
    val playerListViewModel: PlayerListViewModel.Factory
    val trackDetailsViewModel: TrackDetailsViewModel.Factory
    val trackListViewModel: TrackListViewModel.Factory
    val warTrackResultViewModel: WarTrackResultViewModel.Factory
    val statsRankingViewModel: StatsRankingViewModel.Factory
    val playerProfileViewModel: PlayerProfileViewModel.Factory
    val teamProfileViewModel: TeamProfileViewModel.Factory
    val currentWarViewModel: CurrentWarViewModel.Factory
}