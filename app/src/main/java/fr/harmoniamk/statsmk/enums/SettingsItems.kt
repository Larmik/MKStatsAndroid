package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R

enum class ListItemType {
    settings, stats, profile, registry
}


sealed class MenuItems(val titleRes: Int, val route: String? = null, val type: ListItemType) {
   class ManagePlayers() : MenuItems(R.string.mon_quipe, "Home/Settings/Team", ListItemType.registry)
   class ManageTeams() : MenuItems(R.string.g_rer_les_quipes, "Home/Settings/Opponents", ListItemType.registry)
   class Players() : MenuItems(R.string.joueurs_libres, "Home/Settings/Players", ListItemType.registry)
   class Profile() : MenuItems(R.string.profil, "Home/Settings/Profile", ListItemType.registry)
   class IndivStats(val userId: String) : MenuItems(R.string.statistiques_individuelles, "Home/Stats/Players/$userId", ListItemType.stats)
   class TeamStats() : MenuItems(R.string.statistiques_de_l_quipe, "Home/Stats/Team", ListItemType.stats)
   class PlayerStats() : MenuItems(R.string.statistiques_des_joueurs, "Home/Stats/Players", ListItemType.stats)
   class OpponentStats() : MenuItems(R.string.statistiques_des_adversaires, "Home/Stats/Opponents", ListItemType.stats)
   class MapStats() : MenuItems(R.string.statistiques_des_circuits, "Home/Stats/Maps/Ranking", ListItemType.stats)
   class ChangeMail() : MenuItems(R.string.changer_mon_email, null, ListItemType.profile)
   class ChangePassword() : MenuItems(R.string.changer_mon_mot_de_passe, null, ListItemType.profile)
   class ChangePicture() : MenuItems(R.string.changer_ma_photo_de_profil, null, ListItemType.profile)
   class Logout() : MenuItems(R.string.se_d_connecter, null, ListItemType.profile)
   class Refresh() : MenuItems(R.string.refresh_data, null, ListItemType.settings)
}