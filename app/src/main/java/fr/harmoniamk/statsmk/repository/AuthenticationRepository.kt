package fr.harmoniamk.statsmk.repository

import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.ResetPasswordResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

interface AuthenticationRepositoryInterface {
    fun createUser(email: String, password: String): Flow<AuthUserResponse>
    fun signIn(email: String, password: String): Flow<AuthUserResponse>
    fun reauthenticate(email: String, password: String): Flow<AuthUserResponse>
    fun signOut()
    fun resetPassword(email: String): Flow<ResetPasswordResponse>
    fun updateProfile(username: String, imageUrl: String?) : Flow<Unit>
    fun updateEmail(email: String): Flow<Unit>
    val user: FirebaseUser?
    val userRole: Int
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface AuthenticationRepositoryModule {
    @Binds
    @Singleton
    fun bindRepository(impl: AuthenticationRepository): AuthenticationRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class AuthenticationRepository @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface) : AuthenticationRepositoryInterface {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    override fun createUser(email: String, password: String): Flow<AuthUserResponse> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (isActive && task.isSuccessful) trySend(AuthUserResponse.Success(auth.currentUser))
            }
            .addOnFailureListener {
                trySend(AuthUserResponse.Error(it.localizedMessage ?: it.message.toString()))
            }
        awaitClose {  }
    }

    override fun signIn(email: String, password: String): Flow<AuthUserResponse>  = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (isActive && task.isSuccessful) trySend(AuthUserResponse.Success(auth.currentUser))
            }
            .addOnFailureListener {
                trySend(AuthUserResponse.Error(it.localizedMessage ?: it.message.toString()))
            }

        awaitClose {  }
    }

    override fun reauthenticate(email: String, password: String): Flow<AuthUserResponse> = callbackFlow {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (isActive) {
            when (val user = auth.currentUser) {
                null -> trySend(AuthUserResponse.Error("No user found"))
                else -> user.reauthenticate(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful)
                            trySend(AuthUserResponse.Success(user))
                        else trySend(AuthUserResponse.Error("Reauth task has failed"))
                    }
                    .addOnFailureListener { trySend(AuthUserResponse.Error(it.localizedMessage ?: it.message.toString())) }

            }
        }


        awaitClose {  }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun resetPassword(email: String) = callbackFlow {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (isActive && task.isSuccessful) trySend(ResetPasswordResponse.Success("Email envoyé"))
            }
            .addOnFailureListener {
                if (isActive) trySend(ResetPasswordResponse.Error(it.localizedMessage ?: it.message.toString()))
            }
        awaitClose {  }
    }

    override fun updateProfile(username: String, imageUrl: String?) = callbackFlow {
        auth.currentUser?.let {
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
            imageUrl?.let {
                profileUpdate.setPhotoUri(Uri.parse(it))
            }
            it.updateProfile(profileUpdate.build()).addOnCompleteListener { task ->
                if (task.isSuccessful) trySend(Unit)
            }.addOnFailureListener {
                trySend(Unit)
            }
        }
        awaitClose {  }
    }

    override fun updateEmail(email: String) = callbackFlow {
        auth.currentUser?.let {
            it.updateEmail(email).addOnCompleteListener { task ->
                if (isActive && task.isSuccessful) trySend(Unit)
            }.addOnFailureListener {
                if (isActive) trySend(Unit)
            }
        }
        awaitClose {  }
    }

    override val user: FirebaseUser?
        get() = auth.currentUser

    override val userRole: Int
        get() = preferencesRepository.role
}