package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R

enum class ListItemType {
    settings, stats, profile, registry, options
}


sealed class MenuItems(val titleRes: Int, val route: String? = null, val type: ListItemType) {

   class ManagePlayers() : MenuItems(R.string.mon_quipe, "Home/Registry/Team", ListItemType.registry)
   class ManageTeams() : MenuItems(R.string.g_rer_les_quipes, "Home/Registry/Opponents", ListItemType.registry)
   class Players() : MenuItems(R.string.joueurs_libres, "Home/Registry/Players", ListItemType.registry)
   class Profile() : MenuItems(R.string.profil, "Home/Registry/Profile", ListItemType.registry)
   class Settings() : MenuItems(R.string.settings, "Home/Registry/Settings", ListItemType.registry)

   class IndivStats(val userId: String) : MenuItems(R.string.statistiques_individuelles, "Home/Stats/Players/$userId", ListItemType.stats)
   class TeamStats() : MenuItems(R.string.statistiques_de_l_quipe, "Home/Stats/Team", ListItemType.stats)
   class PlayerStats() : MenuItems(R.string.statistiques_des_joueurs, "Home/Stats/Players", ListItemType.stats)
   class OpponentStats() : MenuItems(R.string.statistiques_des_adversaires, "Home/Stats/Opponents", ListItemType.stats)
   class MapStats() : MenuItems(R.string.statistiques_des_circuits, "Home/Stats/Maps/Ranking/Periodic/All", ListItemType.stats)

   class ChangeMail() : MenuItems(R.string.changer_mon_email, null, ListItemType.profile)
   class ChangePassword() : MenuItems(R.string.changer_mon_mot_de_passe, null, ListItemType.profile)
   class ChangePicture() : MenuItems(R.string.changer_ma_photo_de_profil, null, ListItemType.profile)
   class Logout() : MenuItems(R.string.se_d_connecter, null, ListItemType.profile)

   class Theme() : MenuItems(R.string.theme, null, ListItemType.settings)
   class Refresh() : MenuItems(R.string.refresh_data, null, ListItemType.settings)
   class FetchTags() : MenuItems(R.string.fetch_teams, null, ListItemType.settings)
   class PurgeUsers() : MenuItems(R.string.purge_users, null, ListItemType.settings)
   class Help(): MenuItems(R.string.help, "Home/Registry/Settings/Help", ListItemType.settings)
   class StatsDisplayMode(): MenuItems(R.string.stats_display_mode, null, ListItemType.settings)
   class Credit(): MenuItems(R.string.credits, "Home/Registry/Settings/Credits", ListItemType.settings)
   class Coffee() : MenuItems(R.string.coffee, null, ListItemType.settings)
}