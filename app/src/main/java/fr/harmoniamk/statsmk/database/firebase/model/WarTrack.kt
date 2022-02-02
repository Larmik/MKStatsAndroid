package fr.harmoniamk.statsmk.database.firebase.model

data class WarTrack(
    val mid: String? = null,
    val warId: String? = null,
    var positions: List<WarPosition>? = null,
)