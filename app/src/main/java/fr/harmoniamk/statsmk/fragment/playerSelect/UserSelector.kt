package fr.harmoniamk.statsmk.fragment.playerSelect

import fr.harmoniamk.statsmk.model.firebase.User

data class UserSelector(val user: User? = null, var isSelected: Boolean? = null, val isCategory: Boolean = false)
