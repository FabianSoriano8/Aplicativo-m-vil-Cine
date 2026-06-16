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
    val trailerUrl: String = "",
    val genres: List<String> = emptyList(),
    val releaseYear: String = "",
    val director: String = "",
    @com.google.firebase.firestore.Exclude
    val progress: Int = 0
)