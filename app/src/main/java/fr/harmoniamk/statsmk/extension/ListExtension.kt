package fr.harmoniamk.statsmk.extension

import fr.harmoniamk.statsmk.database.firebase.model.War

fun List<War>.getLasts(teamId: String?) = this.filter { war -> war.isOver && war.teamHost == teamId }.sortedByDescending{ it.createdDate }
fun List<War>.getCurrent(teamId: String?) = this.singleOrNull { war -> !war.isOver && war.teamHost == teamId }