package fr.harmoniamk.statsmk.model.local

sealed class TeamType(val mainTeamId: String?) {
    class SingleRoster(val teamId: String): TeamType(mainTeamId = teamId)
    class MultiRoster(val teamId: String?, val secondaryTeamsId: List<String>?): TeamType(mainTeamId = teamId)
}