package fr.harmoniamk.statsmk.model.firebase

import java.io.Serializable

data class NewWarTrack(
    val mid: String? = null,
    val trackIndex: Int? = null,
    var warPositions: List<NewWarPositions>? = null
) : Serializable