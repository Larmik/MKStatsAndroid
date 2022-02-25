package fr.harmoniamk.statsmk.model

import fr.harmoniamk.statsmk.database.model.Team

data class MKTeam(val team: Team?) {

    val integrationLabel: String = "Intégrer ${team?.name} (${team?.shortName})"

}