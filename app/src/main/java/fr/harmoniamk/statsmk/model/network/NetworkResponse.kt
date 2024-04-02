package fr.harmoniamk.statsmk.model.network

sealed class NetworkResponse<T> {

    class Success<T>(val response: T): NetworkResponse<T>()

    class Error<T>(val error: String): NetworkResponse<T>()
}

