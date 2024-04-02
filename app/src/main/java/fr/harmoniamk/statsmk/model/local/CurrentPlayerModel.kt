package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.model.network.MKPlayer

data class CurrentPlayerModel(val player: MKPlayer?, val score: Int, val tracksPlayed: Int, val shockCount: Int = 0)