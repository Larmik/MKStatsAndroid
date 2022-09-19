package fr.harmoniamk.statsmk.model.firebase

import java.io.Serializable

data class NewWarPositions(
    val mid: String? = null,
    var playerId: String? = null,
    val position: Int? = null
): Serializable