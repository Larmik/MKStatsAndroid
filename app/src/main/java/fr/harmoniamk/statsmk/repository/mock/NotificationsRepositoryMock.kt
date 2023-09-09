package fr.harmoniamk.statsmk.repository.mock

import fr.harmoniamk.statsmk.repository.NotificationsRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NotificationsRepositoryMock : NotificationsRepositoryInterface {
    override fun register(teamTopic: String): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun switchNotification(topic: String, subscribed: Boolean): Flow<Unit> = flow {
        emit(Unit)
    }
}