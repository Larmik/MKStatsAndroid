package fr.harmoniamk.statsmk.model.firebase


sealed class ResetPasswordResponse(val message: String) {
    class Success(text: String) : ResetPasswordResponse(message = text)
    class Error(text: String): ResetPasswordResponse(message = text)
}