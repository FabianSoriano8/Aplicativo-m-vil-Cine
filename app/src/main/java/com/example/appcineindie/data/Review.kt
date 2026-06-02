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
    val timestamp: Any? = null
)