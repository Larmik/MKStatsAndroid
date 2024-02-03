package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.model.network.MKCLightPlayer

data class CurrentPlayerModel(val player: MKCLightPlayer?, val score: Int, val tracksPlayed: Int, val shockCount: Int = 0)