package fr.harmoniamk.statsmk.model.firebase

import java.io.Serializable

data class NewWarTrack(
    val mid: String? = null,
    var trackIndex: Int? = null,
    var warPositions: List<NewWarPositions>? = null,
    var shocks: List<Shock>? = null
) : Serializable {
    companion object
}