package fr.harmoniamk.statsmk.fragment.managePlayers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map

class EditRoleViewModel: ViewModel() {

    private val _sharedRole = MutableSharedFlow<Int?>()
    val sharedRole = _sharedRole.asSharedFlow()

    fun bind(onRoleChange: Flow<Int>) {
        onRoleChange
            .map {
                when (it) {
                    R.id.member -> UserRole.MEMBER.ordinal
                    R.id.admin -> UserRole.ADMIN.ordinal
                    R.id.leader -> UserRole.LEADER.ordinal
                    else -> null
                }
            }.bind(_sharedRole, viewModelScope)
    }

}