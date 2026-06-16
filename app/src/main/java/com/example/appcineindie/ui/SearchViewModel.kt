package com.example.appcineindie.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appcineindie.data.Movie
import com.google.firebase.firestore.FirebaseFirestore

class SearchViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    
    private val _allMovies = MutableLiveData<List<Movie>>()
    private val _filteredMovies = MutableLiveData<List<Movie>>()
    val filteredMovies: LiveData<List<Movie>> get() = _filteredMovies

    fun fetchAllMovies() {
        db.collection("movies")
            .get()
            .addOnSuccessListener { result ->
                val movies = result.toObjects(Movie::class.java)
                _allMovies.value = movies
                _filteredMovies.value = movies
            }
    }

    fun filterMovies(query: String) {
        val all = _allMovies.value ?: return
        if (query.isEmpty()) {
            _filteredMovies.value = all
        } else {
            _filteredMovies.value = all.filter { movie ->
                movie.title.contains(query, ignoreCase = true) ||
                        movie.category.contains(query, ignoreCase = true) ||
                        movie.director.contains(query, ignoreCase = true) || // Búsqueda por director
                        movie.genres.any { it.contains(query, ignoreCase = true) }
            }
        }
    }
}