package fr.harmoniamk.statsmk.database.firebase.model

import com.google.firebase.database.IgnoreExtraProperties

const val TOTAL_TRACKS = 12
const val TOTAL_WAR_SCORE = 984
const val TOTAL_TRACK_SCORE = 82

data class War(
    val mid: String? = null,
    val name: String? = null,
    val playerHostId: String? = null,
    val teamHost: String? = null,
    val teamOpponent: String? = null,
    var scoreHost: Int = 0,
    var scoreOpponent: Int = 0,
    val createdDate: String? = null,
    var updatedDate: String? = null
) {

    val trackPlayed = (TOTAL_TRACKS - ( TOTAL_WAR_SCORE - ( scoreHost + scoreOpponent)) / TOTAL_TRACK_SCORE)
    val isOver = trackPlayed == 12
}