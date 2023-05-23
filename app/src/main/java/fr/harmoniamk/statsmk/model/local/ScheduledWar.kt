package fr.harmoniamk.statsmk.model.local

data class ScheduledWar(
    val opponentId: String,
    val lineUp: List<String>,
    val hour: Int
)