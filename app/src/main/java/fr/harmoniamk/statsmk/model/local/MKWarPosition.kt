package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.User

class MKWarPosition(val position: NewWarPositions, val player: User? = null)