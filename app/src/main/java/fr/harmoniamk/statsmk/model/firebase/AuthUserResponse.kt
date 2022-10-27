package fr.harmoniamk.statsmk.model.firebase

import com.google.firebase.auth.FirebaseUser

sealed class AuthUserResponse {
    class Success(val user : FirebaseUser?) : AuthUserResponse()
    class Error(val message: String): AuthUserResponse()
}