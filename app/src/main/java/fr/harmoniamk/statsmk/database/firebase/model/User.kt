package fr.harmoniamk.statsmk.database.firebase.model

data class User(
    val name: String? = null,
    val accessCode: String? = null,
    val team: Int? = null
) : FirebaseObject()