package com.example.appcineindie.data

import com.google.firebase.firestore.DocumentId

data class Review(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val movieId: String = "",
    val movieTitle: String = "",
    val comment: String = "",
    val rating: Any? = null,
    val timestamp: Any? = null,
    val replies: List<Reply> = emptyList()
) {
    /**
     * Verifica si la reseña pertenece al usuario indicado.
     */
    fun isOwnedBy(uid: String?) = userId == uid
}

data class Reply(
    val userId: String = "",
    val userName: String = "",
    val comment: String = "",
    val timestamp: Any? = null
) {
    /**
     * Verifica si la respuesta pertenece al usuario indicado.
     */
    fun isOwnedBy(uid: String?) = userId == uid
}
