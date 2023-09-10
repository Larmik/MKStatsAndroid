package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R

interface SortType {
    val resId: Int
}

interface FilterType {
    val resId: Int
}


enum class TrackSortType(override val resId: Int) : SortType {
    TOTAL_PLAYED(R.string.nb_fois_jou_es),
    TOTAL_WIN(R.string.nb_fois_gagn_es),
    WINRATE(R.string.winrate),
    AVERAGE_DIFF(R.string.moyenne_diff)
}
enum class WarSortType(override val resId: Int) : SortType  {
    DATE(R.string.date),
    TEAM(R.string.adversaire),
    SCORE(R.string.score_diff)
}
enum class WarFilterType(override val resId: Int) : FilterType {
    WEEK(R.string.cette_semaine),
    OFFICIAL(R.string.officielles),
    PLAY(R.string.participation)
}
enum class PlayerSortType(override val resId: Int) : SortType {
    NAME(R.string.nom),
    WINRATE(R.string.winrate),
    AVERAGE(R.string.score_moyen),
    TOTAL_WIN(R.string.wars_jou_es)
}