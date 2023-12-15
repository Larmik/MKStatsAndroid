package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R

sealed class BottomNavItem(var title: Int, var icon:Int, var route :String){

    object War : BottomNavItem(R.string.team_war, R.drawable.teamwar,"Home/War")
    object Stats: BottomNavItem(R.string.stats,R.drawable.stats,"Home/Stats")
    object Settings: BottomNavItem(R.string.registry, R.drawable.registry,"Home/Settings")
}