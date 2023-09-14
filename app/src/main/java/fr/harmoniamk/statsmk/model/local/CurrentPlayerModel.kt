package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.model.firebase.User

data class CurrentPlayerModel(val player: User?, val score: Int, val isOld: Boolean? = null, val isNew: Boolean? = null, val shockCount: Int = 0)