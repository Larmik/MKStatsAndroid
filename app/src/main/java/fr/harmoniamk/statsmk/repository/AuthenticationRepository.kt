package fr.harmoniamk.statsmk.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.ResetPasswordResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import javax.inject.Inject

interface AuthenticationRepositoryInterface {
    fun createUser(email: String, password: String): Flow<AuthUserResponse>
    fun signIn(email: String, password: String): Flow<AuthUserResponse>
    fun reauthenticate(email: String, password: String): Flow<AuthUserResponse>
    fun signOut()
    fun resetPassword(email: String): Flow<ResetPasswordResponse>
    fun updateProfile(username: String, imageUrl: String?) : Flow<Unit>
    fun updateEmail(email: String): Flow<Unit>
    val user: FirebaseUser?
    val isAdmin: Flow<Boolean>
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
class AuthenticationRepository @Inject constructor(@ApplicationContext private val context: Context) : AuthenticationRepositoryInterface {

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

    override fun updateProfile(username: String, imageUrl: String?) = callbackFlow {
        auth.currentUser?.let {
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
            imageUrl?.let {
                profileUpdate.setPhotoUri(Uri.parse(it))
            }
            it.updateProfile(profileUpdate.build()).addOnCompleteListener { task ->
                if (isActive && task.isSuccessful) offer(Unit)
            }.addOnFailureListener {
                if (isActive) offer(Unit)
            }
        }
        awaitClose {  }
    }

    override fun updateEmail(email: String) = callbackFlow {
        auth.currentUser?.let {
            it.updateEmail(email).addOnCompleteListener { task ->
                if (isActive && task.isSuccessful) offer(Unit)
            }.addOnFailureListener {
                if (isActive) offer(Unit)
            }
        }
        awaitClose {  }
    }

    override val user: FirebaseUser?
        get() = auth.currentUser

    override val isAdmin: Flow<Boolean>
        get() = flowOf(auth.currentUser?.uid)
            .flatMapLatest { FirebaseRepository(context).getUser(it) }
            .mapNotNull { it?.role }
            .mapNotNull { it >= UserRole.ADMIN.ordinal }

}