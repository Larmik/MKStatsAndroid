package fr.harmoniamk.statsmk.database.firebase.model

data class User(
    val mid: String?,
    val name: String? = null,
    var accessCode: String? = null,
    var team: String? = null,
    var currentWar: String? = null
)