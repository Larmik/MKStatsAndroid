package fr.harmoniamk.statsmk.fragment.playerSelect

import fr.harmoniamk.statsmk.model.network.MKPlayer

data class UserSelector(val user: MKPlayer? = null, var isSelected: Boolean? = null, val isCategory: Boolean = false)
