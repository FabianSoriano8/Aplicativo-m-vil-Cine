package com.example.appcineindie.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appcineindie.data.Movie
import com.google.firebase.firestore.Query

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _featuredMovie = MutableLiveData<Movie?>()
    val featuredMovie: LiveData<Movie?> get() = _featuredMovie
    private var manualMovies = listOf<Movie>()
    private var progressMovies = listOf<Movie>()

    private fun combineAndShow() {
        // Une ambas listas eliminando duplicados por ID
        val combined = (progressMovies + manualMovies).distinctBy { it.id }
        _continueWatchingMovies.postValue(combined)
    }
    
    private val _trendingMovies = MutableLiveData<List<Movie>>()
    val trendingMovies: LiveData<List<Movie>> get() = _trendingMovies

    private val _continueWatchingMovies = MutableLiveData<List<Movie>>()
    val continueWatchingMovies: LiveData<List<Movie>> get() = _continueWatchingMovies

    fun fetchHomeData(userId: String) {
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

        // Carga manual (Editorial)
        db.collection("movies").whereEqualTo("category", "Continue").get()
            .addOnSuccessListener {
                manualMovies = it.toObjects(Movie::class.java)
                combineAndShow()
            }
        fetchContinueWatching(userId)
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

    fun fetchContinueWatching(userId: String) {
        Log.d("HomeViewModel", "Cargando ContinueWatching para usuario: $userId")
        db.collection("users").document(userId).collection("progress")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("HomeViewModel", "Error en snapshotListener", error)
                    return@addSnapshotListener
                }
                
                if (snapshots == null || snapshots.isEmpty) {
                    Log.d("HomeViewModel", "No hay progreso guardado para este usuario.")
                    progressMovies = emptyList()
                    combineAndShow()
                    return@addSnapshotListener
                }

                Log.d("HomeViewModel", "Documentos de progreso encontrados: ${snapshots.size()}")
                val progressMap = snapshots.documents.associate { 
                    it.id to mapOf(
                        "position" to (it.getLong("position") ?: 0L),
                        "duration" to (it.getLong("duration") ?: 0L)
                    )
                }
                
                db.collection("movies")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), progressMap.keys.toList())
                    .get()
                    .addOnSuccessListener { result ->
                        val movies = result.toObjects(Movie::class.java).map { movie ->
                            val data = progressMap[movie.id]
                            val pos = data?.get("position") ?: 0L
                            val realDuration = data?.get("duration") ?: 0L
                            
                            // Priorizamos la duración real guardada por el player, 
                            // si no existe usamos la del string del metadato
                            val total = if (realDuration > 0) realDuration else parseDurationToMillis(movie.duration)
                            
                            // Cálculo con Double para precisión
                            val percent = if (total > 0) ((pos.toDouble() / total.toDouble()) * 100.0).toInt() else 0
                            
                            Log.d("HomeViewModel", "CALCULO -> Pelicula: ${movie.title}, Pos: $pos, Total: $total, Percent: $percent")

                            // Aseguramos que si hay progreso, la barra muestre al menos un 1%
                            val finalProgress = if (pos > 0) percent.coerceIn(1, 100) else 0
                            movie.copy(progress = finalProgress)
                        }
                        progressMovies = movies
                        combineAndShow()
                    }
                    .addOnFailureListener { Log.e("HomeViewModel", "Error cargando peliculas de progreso", it) }
            }
    }
    // Función auxiliar para calcular el porcentaje real
    private fun parseDurationToMillis(duration: String): Long {
        return try {
            var totalMillis = 0L
            if (duration.contains("h")) {
                val hours = duration.substringBefore("h").trim().toLong()
                totalMillis += hours * 3600 * 1000
            }
            if (duration.contains("m")) {
                val minutes = if (duration.contains("h")) {
                    duration.substringAfter("h").substringBefore("m").trim().toLong()
                } else {
                    duration.substringBefore("m").trim().toLong()
                }
                totalMillis += minutes * 60 * 1000
            }
            if (totalMillis == 0L) 3600000L else totalMillis
        } catch (e: Exception) {
            3600000L
        } // 1 hora por defecto si falla
    }
}