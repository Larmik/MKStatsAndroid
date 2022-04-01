package fr.harmoniamk.statsmk.database.model

data class User(
    val mid: String?,
    var name: String? = null,
    var accessCode: String? = null,
    var team: String? = null,
    var currentWar: String? = null,
    var isAdmin: Boolean? = null
)