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
    val videoUrl: String = "",
    val genres: List<String> = emptyList(),
    @com.google.firebase.firestore.Exclude
    val progress: Int = 0 // Porcentaje de 0 a 100
) {
    // Firebase necesita un constructor vacío obligatorio (valores por defecto)
}