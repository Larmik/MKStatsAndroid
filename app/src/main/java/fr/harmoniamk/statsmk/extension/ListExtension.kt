package fr.harmoniamk.statsmk.extension

import fr.harmoniamk.statsmk.model.MKWar

fun List<MKWar>.getLasts(teamId: String?) = this.filter { war -> war.isOver && war.war?.teamHost == teamId }.sortedByDescending{ it.war?.createdDate }
fun List<MKWar>.getCurrent(teamId: String?) = this.singleOrNull { war -> !war.isOver && war.war?.teamHost == teamId }
fun List<MKWar>.getBests(teamId: String?) = this.filter { war -> war.isOver && war.war?.teamHost == teamId  }.sortedWith(compareBy<MKWar> { it.war?.scoreHost }.thenBy { it.displayedAverage.toInt() }).reversed()
fun List<Int?>.sum(): Int {
    var sum = 0
    this.filterNotNull().forEach { sum += it }
    return sum
}