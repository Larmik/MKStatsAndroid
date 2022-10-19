package fr.harmoniamk.statsmk.model.firebase

import java.io.Serializable

data class NewWar(
    val mid: String? = null,
    val playerHostId: String? = null,
    val teamHost: String? = null,
    val teamOpponent: String? = null,
    val createdDate: String? = null,
    var warTracks: List<NewWarTrack>? = null,
    var penalties: List<Penalty>? = null,
    val isOfficial: Boolean? = null
) : Serializable