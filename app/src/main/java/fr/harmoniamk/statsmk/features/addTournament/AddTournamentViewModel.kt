package fr.harmoniamk.statsmk.features.addTournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.room.model.Tournament
import fr.harmoniamk.statsmk.repository.TournamentRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AddTournamentViewModel @Inject constructor(private val tournamentRepository: TournamentRepositoryInterface) :
    ViewModel() {

    private val _sharedClose = MutableSharedFlow<Unit>()
    val sharedClose = _sharedClose.asSharedFlow()

    fun bind(
        onNameAdded: Flow<String>,
        onTotalTrackAdded: Flow<Int>,
        onDifficultyAdded: Flow<Int>,
        onButtonClick: Flow<Unit>
    ) {

        var name: String? = null
        var tracks = 12
        var difficulty = 1
        val date = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(Date())


        onNameAdded.filterNot { it.isEmpty() }.onEach { name = it }.launchIn(viewModelScope)
        onTotalTrackAdded.onEach { tracks = it }.launchIn(viewModelScope)
        onDifficultyAdded.onEach { difficulty = it }.launchIn(viewModelScope)
        onButtonClick
            .mapNotNull { name }
            .map {
                Tournament(
                    name = it,
                    trackCount = idToValue(tracks),
                    difficulty = idToValue(difficulty),
                    createdDate = date,
                    updatedDate = date
                )
            }
            .flatMapLatest { tournamentRepository.insert(it) }
            .onEach { _sharedClose.emit(Unit) }
            .launchIn(viewModelScope)

    }

    private fun idToValue(id: Int) = when (id) {
        R.id.mirror -> 2
        R.id.hard -> 3
        R.id.gp1 -> 4
        R.id.gp2 -> 8
        R.id.gp3, 12 -> 12
        R.id.gp4 -> 16
        R.id.gp5 -> 20
        R.id.gp6 -> 24
        else -> 1
    }


}
