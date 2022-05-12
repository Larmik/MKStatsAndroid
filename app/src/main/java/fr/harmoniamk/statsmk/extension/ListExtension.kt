package fr.harmoniamk.statsmk.extension

import fr.harmoniamk.statsmk.model.local.MKWar

fun List<MKWar>.getLasts(teamId: String?) = this.filter {
        war -> war.isOver && war.war?.teamHost == teamId
}.sortedByDescending{ it.war?.createdDate?.formatToDate() }.safeSubList(0, 5)
fun List<MKWar>.getCurrent(teamId: String?) = this.singleOrNull { war -> !war.isOver && war.war?.teamHost == teamId }
fun List<MKWar>.getBests(teamId: String?) = this.filter { war -> war.isOver && war.war?.teamHost == teamId  }.sortedWith(compareBy<MKWar> { it.scoreHost }.thenBy { it.displayedAverage.toInt() }).reversed().safeSubList(0, 3)
fun List<Int?>?.sum(): Int {
    var sum = 0
    this?.filterNotNull()?.forEach { sum += it }
    return sum
}

fun <T> List<T>.safeSubList(from: Int, to: Int): List<T> = when {
    this.size < to -> this
    else -> this.subList(from, to)
}

fun Any.toMapList(): List<Map<*,*>>? = this as? List<Map<*,*>>