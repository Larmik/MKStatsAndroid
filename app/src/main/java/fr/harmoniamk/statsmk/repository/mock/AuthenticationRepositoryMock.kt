package fr.harmoniamk.statsmk.repository.mock

import com.google.firebase.auth.FirebaseUser
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.ResetPasswordResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthenticationRepositoryMock : AuthenticationRepositoryInterface {
    override fun createUser(email: String, password: String): Flow<AuthUserResponse> = flow {
        emit(AuthUserResponse.Success(null))
    }

    override fun signIn(email: String, password: String): Flow<AuthUserResponse> = flow {
        emit(AuthUserResponse.Success(null))
    }

    override fun reauthenticate(email: String, password: String): Flow<AuthUserResponse> = flow {
        emit(AuthUserResponse.Success(null))
    }

    override fun signOut() {
    }

    override fun resetPassword(email: String): Flow<ResetPasswordResponse> = flow {
        emit(ResetPasswordResponse.Success(""))
    }

    override fun updateProfile(username: String, imageUrl: String?): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun updateEmail(email: String): Flow<Unit> = flow {
        emit(Unit)
    }

    override val user: FirebaseUser?
        get() = null
    override val userRole: Int
        get() = 0
}