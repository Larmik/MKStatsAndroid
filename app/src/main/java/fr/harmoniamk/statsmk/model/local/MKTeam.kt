package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.model.firebase.Team

data class MKTeam(val team: Team?) {

    val integrationLabel: String = "Intégrer ${team?.name} (${team?.shortName})"

}