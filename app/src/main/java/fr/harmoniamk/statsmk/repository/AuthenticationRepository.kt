package fr.harmoniamk.statsmk.repository

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.ResetPasswordResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import javax.inject.Inject

interface AuthenticationRepositoryInterface {
    fun createUser(email: String, password: String): Flow<AuthUserResponse>
    fun signIn(email: String, password: String): Flow<AuthUserResponse>
    fun reauthenticate(email: String, password: String): Flow<AuthUserResponse>
    fun signOut()
    fun resetPassword(email: String): Flow<ResetPasswordResponse>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
interface AuthenticationRepositoryModule {
    @Binds
    fun bindRepository(impl: AuthenticationRepository): AuthenticationRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class AuthenticationRepository @Inject constructor() : AuthenticationRepositoryInterface {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    override fun createUser(email: String, password: String): Flow<AuthUserResponse> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (isActive && task.isSuccessful) offer(AuthUserResponse.Success(auth.currentUser))
            }
            .addOnFailureListener {
                Log.d("MKDebug", "createUser: ${it.message}")
                offer(AuthUserResponse.Error(it.localizedMessage ?: it.message.toString()))
            }
        awaitClose {  }
    }

    override fun signIn(email: String, password: String): Flow<AuthUserResponse>  = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (isActive && task.isSuccessful) offer(AuthUserResponse.Success(auth.currentUser))
            }
            .addOnFailureListener {
                Log.d("MKDebug", "createUser: ${it.message}")
                offer(AuthUserResponse.Error(it.localizedMessage ?: it.message.toString()))
            }

        awaitClose {  }
    }

    override fun reauthenticate(email: String, password: String): Flow<AuthUserResponse> = callbackFlow {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (isActive) {
            if (auth.currentUser == null) offer(AuthUserResponse.Error("No user found"))
                auth.currentUser?.let { user ->
                    user.reauthenticate(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                                offer(AuthUserResponse.Success(user))
                            else offer(AuthUserResponse.Error("Reauth task has failed"))
                        }
                        .addOnFailureListener { offer(AuthUserResponse.Error(it.localizedMessage ?: it.message.toString())) }
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
                if (isActive && task.isSuccessful) offer(ResetPasswordResponse.Success("Email envoyé"))
            }
            .addOnFailureListener {
                if (isActive) offer(ResetPasswordResponse.Error(it.localizedMessage ?: it.message.toString()))
            }
        awaitClose {  }
    }

}