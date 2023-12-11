package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer

class MKWarPosition(val position: NewWarPositions, val player: User? = null, val mkcPlayer: MKCLightPlayer? = null)