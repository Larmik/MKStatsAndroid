package fr.harmoniamk.statsmk.fragment.currentWar

import fr.harmoniamk.statsmk.model.firebase.User

data class CurrentPlayerModel(val player: User?, val score: Int, val isOld: Boolean? = null, val isNew: Boolean? = null) {



}