package com.example.appcineindie.data

import com.google.firebase.firestore.DocumentId

data class Movie(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val duration: String = "",
    val rating: String = "",
    val remainingTime: String = "",
    val genres: List<String> = emptyList()
) {
    // Firebase necesita un constructor vacío obligatorio (valores por defecto)
}