package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R

enum class ListItemType {
    settings, stats
}

enum class ListItems(val titleRes: Int, val route: String, val type: ListItemType) {
    manage_players(R.string.mon_quipe, "Settings/ManagePlayers", ListItemType.settings),
    manage_teams(R.string.g_rer_les_quipes, "Settings/ManageTeams", ListItemType.settings),
    players(R.string.joueurs_libres, "Settings/Players", ListItemType.settings),
    profile(R.string.profil, "Settings/Profile", ListItemType.settings),
    indiv_stats(R.string.statistiques_individuelles, "Stats/Indiv", ListItemType.stats),
    team_stats(R.string.statistiques_de_l_quipe, "Stats/Team", ListItemType.stats),
    players_stats(R.string.statistiques_des_joueurs, "Stats/Players", ListItemType.stats),
    opponent_stats(R.string.statistiques_des_adversaires, "Stats/Opponents", ListItemType.stats),
    map_stats(R.string.statistiques_des_circuits, "Stats/Maps", ListItemType.stats),
    periodic_stats(R.string.statistiques_p_riodiques, "Stats/Periodic", ListItemType.stats)
}