package fr.harmoniamk.statsmk.enums



enum class WelcomeScreen {
    Signup, Login,  Home;

    companion object {
        @JvmStatic
        fun getFromName(name: String?) = values().firstOrNull { it.name == name }
    }
}