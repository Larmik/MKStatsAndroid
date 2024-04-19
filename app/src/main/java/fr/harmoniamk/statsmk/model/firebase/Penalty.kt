package fr.harmoniamk.statsmk.model.firebase

import java.io.Serializable

data class Penalty(val teamId: String, val amount: Int): Serializable {
    var teamName: String? = null
    var teamShortName: String? = null
}