package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R

enum class ListItemType {
    settings, stats
}

enum class ListItems(val titleRes: Int, val route: String, val type: ListItemType) {
    manage_players(R.string.mon_quipe, "Home/Settings/ManagePlayers", ListItemType.settings),
    manage_teams(R.string.g_rer_les_quipes, "Home/Settings/ManageTeams", ListItemType.settings),
    players(R.string.joueurs_libres, "Home/Settings/Players", ListItemType.settings),
    profile(R.string.profil, "Home/Settings/Profile", ListItemType.settings),
    indiv_stats(R.string.statistiques_individuelles, "Home/Stats/Indiv", ListItemType.stats),
    team_stats(R.string.statistiques_de_l_quipe, "Home/Stats/Team", ListItemType.stats),
    players_stats(R.string.statistiques_des_joueurs, "Home/Stats/Players", ListItemType.stats),
    opponent_stats(R.string.statistiques_des_adversaires, "Home/Stats/Opponents", ListItemType.stats),
    map_stats(R.string.statistiques_des_circuits, "Home/Stats/Maps", ListItemType.stats),
    periodic_stats(R.string.statistiques_p_riodiques, "Home/Stats/Periodic", ListItemType.stats)
}