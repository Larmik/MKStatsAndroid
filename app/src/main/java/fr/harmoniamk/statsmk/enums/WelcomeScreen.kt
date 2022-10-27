package fr.harmoniamk.statsmk.enums

import fr.harmoniamk.statsmk.R


enum class WelcomeScreen(val fragmentResId: Int) {
    WELCOME(R.id.addUserFragment), REAUTH(R.id.reauthUserFragment), CONNECT(R.id.connectUserFragment),  HOME(
        R.id.homeFragment);

    companion object {
        @JvmStatic
        fun getFromName(name: String?) = values().firstOrNull { it.name == name }
    }
}