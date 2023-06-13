package fr.harmoniamk.statsmk.repository

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.service.AlertNotificationService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface NotificationsRepositoryInterface {
    fun register(teamTopic: String): Flow<Unit>
    fun switchNotification (topic: String, subscribed: Boolean): Flow<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface NotificationsRepositoryModule {
    @Binds
    fun bind(impl: NotificationsRepository): NotificationsRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class NotificationsRepository @Inject constructor(@ApplicationContext var context: Context, private val preferencesRepository: PreferencesRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface) : NotificationsRepositoryInterface  {

    override fun register(teamTopic: String) = AlertNotificationService().token
        .onEach {
            databaseRepository.getTeams().firstOrNull()?.let {
                it.map { it.mid }.forEach { teamId ->
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(teamId)
                }
            }
            FirebaseMessaging.getInstance().subscribeToTopic(teamTopic)
        }
        .map { preferencesRepository.fcmToken = it }

    override fun switchNotification(topic: String, subscribed: Boolean) =
        AlertNotificationService().switchNotification(topic, subscribed)
            .filterNotNull()
            .flatMapLatest {
                if (subscribed) databaseRepository.writeTopic(TopicEntity(topic))
                else databaseRepository.deleteTopic(topic)
            }

}