package fr.harmoniamk.statsmk.features.addWar

import fr.harmoniamk.statsmk.database.model.User

data class UserSelector(val user: User, var isSelected: Boolean)
