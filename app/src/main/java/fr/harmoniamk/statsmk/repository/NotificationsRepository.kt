package fr.harmoniamk.statsmk.repository

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.service.AlertNotificationService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface NotificationsRepositoryInterface {
    val register: Flow<Unit>
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
class NotificationsRepository @Inject constructor(@ApplicationContext var context: Context, private val preferencesRepository: PreferencesRepositoryInterface) : NotificationsRepositoryInterface  {

    override val register = AlertNotificationService().token
        .onEach { FirebaseMessaging.getInstance().subscribeToTopic(preferencesRepository.currentTeam?.mid ?: "-1") }
        .map {
            Log.d("FCMTokenMK", it)
            preferencesRepository.fcmToken = it
        }

}