package fr.harmoniamk.statsmk.database.firebase.model

data class User(
    val mid: String?,
    val name: String? = null,
    val accessCode: String? = null,
    var team: String? = null,
)