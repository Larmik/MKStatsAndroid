package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.FilterType
import fr.harmoniamk.statsmk.enums.PlayerSortType
import fr.harmoniamk.statsmk.enums.SortType
import fr.harmoniamk.statsmk.enums.TrackSortType
import fr.harmoniamk.statsmk.enums.WarFilterType
import fr.harmoniamk.statsmk.enums.WarSortType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class Sort(val list: List<SortType>) {
    class TrackSort : Sort(TrackSortType.values().toList())
    class WarSort : Sort(WarSortType.values().toList())
    class PlayerSort : Sort(PlayerSortType.values().toList())
}

sealed class Filter(val list: List<FilterType>) {
    class WarFilter : Filter(WarFilterType.values().toList())
    class None : Filter(listOf())
}


@HiltViewModel
class FilterSortViewModel @Inject constructor() : ViewModel() {

    private val _sortState = MutableStateFlow<SortType?>(null)
    private val _filterState = MutableStateFlow<List<FilterType>?>(null)

    val sortState = _sortState.asStateFlow()
    val filterState = _filterState.asStateFlow()

    private val filters = mutableListOf<FilterType>()


    fun setSortType(sortType: SortType?) {
        _sortState.value = sortType
    }

    fun switchFilter(filter: FilterType, selected: Boolean) {
        val new = mutableListOf<FilterType>()
        new.addAll(filters)
        when (selected) {
            true -> {
                new.remove(filter)
                filters.remove(filter)
            }
            else -> {
                new.add(filter)
                filters.add(filter)
            }
        }
        _filterState.value = new
    }



}