package fr.harmoniamk.statsmk.database.model


data class Team(
    val mid: String?,
    val name: String? = null,
    val shortName: String? = null,
    val accessCode: String? = null
) {

    val integrationLabel: String = "Intégrer $name ($shortName)"
}