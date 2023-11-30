package fr.harmoniamk.statsmk.repository.mock

import fr.harmoniamk.statsmk.repository.RemoteConfigRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteConfigRepositoryMock : RemoteConfigRepositoryInterface {
    override val loadConfig: Flow<Unit>
        get() = flow { emit(Unit) }
    override val minimumVersion: Int
        get() = 0
}