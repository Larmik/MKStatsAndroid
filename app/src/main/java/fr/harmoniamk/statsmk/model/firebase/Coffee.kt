package fr.harmoniamk.statsmk.model.firebase

data class Coffee(
    val date: Long,
    val userId: String,
    val quantity: Int,
    val productId: String
)