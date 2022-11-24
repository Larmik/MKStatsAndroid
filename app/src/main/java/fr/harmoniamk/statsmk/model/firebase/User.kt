package fr.harmoniamk.statsmk.model.firebase

data class User(
    val mid: String?,
    var name: String? = null,
    var team: String? = null,
    var currentWar: String? = null,
    var role: Int? = null,
    var picture: String
)