package fr.harmoniamk.statsmk.features.trackList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.MainApplication
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor() : ViewModel() {

    private val _sharedSearchedItems = MutableSharedFlow<List<Maps>>()
    private val _sharedClick = MutableSharedFlow<Int>()

    val sharedSearchedItems = _sharedSearchedItems.asSharedFlow()
    val sharedClick = _sharedClick.asSharedFlow()

    fun bind(onSearch: Flow<String>, onItemClick: Flow<Maps>) {

        onSearch
            .map { searched ->
                Maps.values().filter {
                    it.name.toLowerCase(Locale.ROOT)
                        .contains(searched.toLowerCase(Locale.ROOT)) || MainApplication.instance?.applicationContext?.getString(
                        it.label
                    )?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
                }
            }
            .bind(_sharedSearchedItems, viewModelScope)

        onItemClick.map { it.ordinal }.bind(_sharedClick, viewModelScope)
    }

}