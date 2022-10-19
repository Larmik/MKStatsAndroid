package fr.harmoniamk.statsmk.model.firebase

data class Penalty(val teamId: String, val amount: Int) {
    var teamName: String? = null
}