package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.network.MKPlayer

class MKWarPosition(val position: NewWarPositions, val mkcPlayer: MKPlayer? = null)