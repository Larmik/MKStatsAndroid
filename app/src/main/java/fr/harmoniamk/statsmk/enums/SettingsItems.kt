package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R

enum class ListItemType {
    settings, stats, profile
}

enum class ListItems(val titleRes: Int, val route: String? = null, val type: ListItemType) {
    manage_players(R.string.mon_quipe, "Home/Settings/Team", ListItemType.settings),
    manage_teams(R.string.g_rer_les_quipes, "Home/Settings/Opponents", ListItemType.settings),
    players(R.string.joueurs_libres, "Home/Settings/Players", ListItemType.settings),
    profile(R.string.profil, "Home/Settings/Profile", ListItemType.settings),
    indiv_stats(R.string.statistiques_individuelles, "Home/Stats/Indiv", ListItemType.stats),
    team_stats(R.string.statistiques_de_l_quipe, "Home/Stats/Team", ListItemType.stats),
    players_stats(R.string.statistiques_des_joueurs, "Home/Stats/Players", ListItemType.stats),
    opponent_stats(R.string.statistiques_des_adversaires, "Home/Stats/Opponents", ListItemType.stats),
    map_stats(R.string.statistiques_des_circuits, "Home/Stats/Maps", ListItemType.stats),
    periodic_stats(R.string.statistiques_p_riodiques, "Home/Stats/Periodic", ListItemType.stats),
    change_pseudo(R.string.changer_mon_pseudo, null, ListItemType.profile),
    change_mail(R.string.changer_mon_email, null, ListItemType.profile),
    change_password(R.string.changer_mon_mot_de_passe, null, ListItemType.profile),
    change_picture(R.string.changer_ma_photo_de_profil, null, ListItemType.profile),
    leave_team(R.string.quitter_mon_quipe, null, ListItemType.profile),
    logout(R.string.se_d_connecter, null, ListItemType.profile)
}