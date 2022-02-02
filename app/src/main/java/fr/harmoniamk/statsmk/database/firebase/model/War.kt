package fr.harmoniamk.statsmk.database.firebase.model

data class War(
    val mid: String? = null,
    val teamHost: String? = null,
    val teamOpponent: String? = null,
    var scoreHost: Int? = null,
    var scoreOpponent: Int? = null,
    val cratedDate: String? = null,
    val updatedDate: String? = null
)