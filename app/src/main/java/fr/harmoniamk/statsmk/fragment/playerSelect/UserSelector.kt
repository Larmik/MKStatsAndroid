package fr.harmoniamk.statsmk.fragment.playerSelect

import fr.harmoniamk.statsmk.model.network.MKCLightPlayer

data class UserSelector(val user: MKCLightPlayer? = null, var isSelected: Boolean? = null, val isCategory: Boolean = false)
