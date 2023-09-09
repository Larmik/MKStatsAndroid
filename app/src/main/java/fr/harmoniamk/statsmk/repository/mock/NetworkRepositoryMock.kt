package fr.harmoniamk.statsmk.repository.mock

import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface

class NetworkRepositoryMock : NetworkRepositoryInterface {
    override val networkAvailable: Boolean
        get() = true
}