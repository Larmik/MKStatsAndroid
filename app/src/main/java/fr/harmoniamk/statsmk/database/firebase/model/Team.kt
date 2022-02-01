package fr.harmoniamk.statsmk.database.firebase.model

data class Team(
    val name: String? = null,
    val shortName: String? = null,
    val accessCode: String? = null
) : FirebaseObject()