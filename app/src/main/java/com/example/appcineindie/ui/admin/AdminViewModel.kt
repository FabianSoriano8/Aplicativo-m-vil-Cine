package com.example.appcineindie.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appcineindie.data.Movie
import com.example.appcineindie.data.Review
import com.google.firebase.firestore.FirebaseFirestore

class AdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _moviesList = MutableLiveData<List<Movie>>()
    val moviesList: LiveData<List<Movie>> get() = _moviesList

    private val _reviewsList = MutableLiveData<List<Review>>()
    val reviewsList: LiveData<List<Review>> get() = _reviewsList

    private val _usersList = MutableLiveData<List<Map<String, Any>>>()
    val usersList: LiveData<List<Map<String, Any>>> get() = _usersList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // --- Movies Management ---

    fun fetchAllMovies() {
        db.collection("movies").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _moviesList.value = snapshot.toObjects(Movie::class.java)
            }
        }
    }

    fun addOrUpdateMovie(movie: Movie, isEdit: Boolean) {
        _isLoading.value = true
        val movieData = hashMapOf(
            "title" to movie.title,
            "description" to movie.description,
            "category" to movie.category,
            "imageUrl" to movie.imageUrl,
            "duration" to movie.duration,
            "rating" to movie.rating,
            "remainingTime" to movie.remainingTime,
            "genres" to movie.genres
        )
        
        if (isEdit) {
            db.collection("movies").document(movie.id).set(movieData)
                .addOnCompleteListener { _isLoading.value = false }
        } else {
            db.collection("movies").add(movieData)
                .addOnCompleteListener { _isLoading.value = false }
        }
    }

    fun deleteMovie(movieId: String) {
        _isLoading.value = true
        db.collection("movies").document(movieId).delete()
            .addOnCompleteListener { _isLoading.value = false }
    }

    // --- Reviews Management ---

    fun fetchAllReviews() {
        db.collection("reviews").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _reviewsList.value = snapshot.toObjects(Review::class.java)
            }
        }
    }

    fun deleteReview(reviewId: String) {
        _isLoading.value = true
        db.collection("reviews").document(reviewId).delete()
            .addOnCompleteListener { _isLoading.value = false }
    }

    // --- Users Management ---

    fun fetchAllUsers() {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val users = snapshot.documents.map { doc ->
                    val map = doc.data?.toMutableMap() ?: mutableMapOf()
                    map["id"] = doc.id
                    map
                }
                _usersList.value = users
            }
        }
    }

    fun deleteUser(userId: String) {
        _isLoading.value = true
        db.collection("users").document(userId).delete()
            .addOnCompleteListener { _isLoading.value = false }
    }

    fun changeUserType(userId: String, newType: String) {
        _isLoading.value = true
        db.collection("users").document(userId).update("type", newType)
            .addOnCompleteListener { _isLoading.value = false }
    }
}