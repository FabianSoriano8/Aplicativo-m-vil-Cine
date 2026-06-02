package com.example.appcineindie.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appcineindie.data.Movie
import com.example.appcineindie.data.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReviewsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _reviewsList = MutableLiveData<List<Review>>()
    val reviewsList: LiveData<List<Review>> get() = _reviewsList

    private val _moviesList = MutableLiveData<List<Movie>>()
    val moviesList: LiveData<List<Movie>> get() = _moviesList

    // 0. Obtener lista de películas para el selector
    fun fetchMovies() {
        android.util.Log.d("ReviewsViewModel", "Iniciando fetch de películas...")
        db.collection("movies").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    android.util.Log.w("ReviewsViewModel", "¡La colección 'movies' está vacía en Firestore!")
                }
                val movies = snapshot.toObjects(Movie::class.java)
                android.util.Log.d("ReviewsViewModel", "Películas cargadas con éxito: ${movies.size}")
                _moviesList.value = movies
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ReviewsViewModel", "Error Firestore al cargar películas: ${e.message}")
            }
    }

    // 1. Escuchar las reseñas (global o de una película específica)
    fun listenForReviews(movieId: String = "") {
        val collectionRef = if (movieId.isEmpty()) {
            db.collection("reviews")
        } else {
            db.collection("movies").document(movieId).collection("reviews")
        }

        // Ordenamos por timestamp descendente (más recientes primero)
        collectionRef.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ReviewsViewModel", "Error Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
                    android.util.Log.d("ReviewsViewModel", "Se encontraron ${reviews.size} reseñas")
                    _reviewsList.value = reviews
                }
            }
    }

    // 2. Guardar una nueva reseña
    fun addReview(movieId: String, review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val collectionRef = if (movieId.isEmpty()) {
            db.collection("reviews")
        } else {
            db.collection("movies").document(movieId).collection("reviews")
        }

        collectionRef.add(review)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}