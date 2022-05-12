package fr.harmoniamk.statsmk.model.firebase

import java.io.Serializable

data class WarTrack(
    val mid: String? = null,
    val warId: String? = null,
    val trackIndex: Int? = null,
    var isOver: Boolean? = null,
    var teamScore: Int? = null
) : Serializable {


}