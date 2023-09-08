package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R

enum class UserRole(val labelId: Int) {
    MEMBER(R.string.membre),
    ADMIN(R.string.admin),
    LEADER(R.string.leader),
    GOD(R.string.god)
}