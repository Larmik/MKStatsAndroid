package fr.harmoniamk.statsmk.database.model

import java.io.Serializable


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
    var trackPlayed: Int = 0,
    val createdDate: String? = null,
    var updatedDate: String? = null
) : Serializable