package com.example.appcineindie.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appcineindie.data.Movie

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _featuredMovie = MutableLiveData<Movie?>()
    val featuredMovie: LiveData<Movie?> get() = _featuredMovie

    private val _trendingMovies = MutableLiveData<List<Movie>>()
    val trendingMovies: LiveData<List<Movie>> get() = _trendingMovies

    private val _continueWatchingMovies = MutableLiveData<List<Movie>>()
    val continueWatchingMovies: LiveData<List<Movie>> get() = _continueWatchingMovies

    fun fetchHomeData() {
        Log.d("HomeViewModel", "Iniciando descarga de datos...")

        // Destacadas
        db.collection("movies")
            .whereEqualTo("category", "Featured")
            .get()
            .addOnSuccessListener { result ->
                val movies = result.toObjects(Movie::class.java)
                Log.d("HomeViewModel", "Featured encontradas: ${movies.size}")
                if (movies.isNotEmpty()) {
                    _featuredMovie.value = movies[0]
                }
            }
            .addOnFailureListener { Log.e("HomeViewModel", "Error cargando Featured", it) }

        // Tendencias
        db.collection("movies")
            .whereEqualTo("category", "Trending")
            .get()
            .addOnSuccessListener { result ->
                val movies = result.toObjects(Movie::class.java)
                Log.d("HomeViewModel", "Trending encontradas: ${movies.size}")
                _trendingMovies.value = movies
            }
            .addOnFailureListener { Log.e("HomeViewModel", "Error cargando Trending", it) }

        // Continuar
        db.collection("movies")
            .whereEqualTo("category", "Continue")
            .get()
            .addOnSuccessListener { result ->
                val movies = result.toObjects(Movie::class.java)
                Log.d("HomeViewModel", "Continue encontradas: ${movies.size}")
                _continueWatchingMovies.value = movies
            }
            .addOnFailureListener { Log.e("HomeViewModel", "Error cargando Continue", it) }
    }

    fun fetchMovieData(movieId: String) {
        db.collection("movies").document(movieId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _featuredMovie.value = document.toObject(Movie::class.java)
                }
            }
            .addOnFailureListener { Log.e("HomeViewModel", "Error cargando pelicula $movieId", it) }
    }
}