package fr.harmoniamk.statsmk.database.model

import java.io.Serializable

data class WarTrack(
    val mid: String? = null,
    val warId: String? = null,
    val trackIndex: Int? = null,
    var isOver: Boolean? = null,
    var teamScore: Int? = null
) : Serializable {


}