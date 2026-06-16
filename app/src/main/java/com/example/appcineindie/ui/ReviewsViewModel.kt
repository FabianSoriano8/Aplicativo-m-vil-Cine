package com.example.appcineindie.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appcineindie.data.Movie
import com.example.appcineindie.data.Review
import com.example.appcineindie.data.repository.CineRepository
import com.google.firebase.firestore.ListenerRegistration

class ReviewsViewModel : ViewModel() {

    private val repository = CineRepository()
    private var reviewsListener: ListenerRegistration? = null

    private var allReviews: List<Review> = emptyList()

    private var currentSortCriterion: Int = 0

    private var currentQuery: String = ""

    private val _reviewsList = MutableLiveData<List<Review>>()
    val reviewsList: LiveData<List<Review>> get() = _reviewsList

    private val _moviesList = MutableLiveData<List<Movie>>()
    val moviesList: LiveData<List<Movie>> get() = _moviesList

    private var movieMap: Map<String, Movie> = emptyMap()

    fun fetchMovies() {
        repository.fetchMovies { movies ->
            _moviesList.value = movies
            movieMap = movies.associateBy { it.id }
            if (currentQuery.isNotEmpty()) applySortAndFilter()
        }
    }

    fun listenForReviews(movieId: String = "") {
        reviewsListener?.remove()
        reviewsListener = repository.listenToReviews(movieId) { reviews ->
            allReviews = reviews
            applySortAndFilter()
        }
    }

    fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.addReview(review, onSuccess, onFailure)
    }

    fun deleteReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.deleteReview(review, onSuccess, onFailure)
    }

    override fun onCleared() {
        super.onCleared()
        reviewsListener?.remove()
    }

    fun filterReviews(query: String) {
        currentQuery = query
        applySortAndFilter()
    }

    fun setSortCriterion(criterion: Int) {
        currentSortCriterion = criterion
        applySortAndFilter()
    }


    private fun applySortAndFilter() {
        val list = if (currentQuery.isEmpty()) {
            allReviews
        } else {
            allReviews.filter { review ->
                val movie = movieMap[review.movieId]
                review.movieTitle.contains(currentQuery, ignoreCase = true) ||
                        review.userName.contains(currentQuery, ignoreCase = true) ||
                        movie?.director?.contains(currentQuery, ignoreCase = true) == true ||
                        movie?.category?.contains(currentQuery, ignoreCase = true) == true ||
                        movie?.genres?.any { it.contains(currentQuery, ignoreCase = true) } == true
            }
        }

        val sorted = when (currentSortCriterion) {
            0 -> list.sortedByDescending { (it.timestamp as? Long) ?: 0L } // Fecha Reciente
            1 -> list.sortedBy { (it.timestamp as? Long) ?: 0L } // Fecha Antigua
            2 -> list.sortedBy { it.userName.lowercase() } // Usuario A-Z
            3 -> list.sortedByDescending { it.userName.lowercase() } // Usuario Z-A
            4 -> list.sortedBy { it.movieTitle.lowercase() } // Película A-Z
            5 -> list.sortedByDescending { it.movieTitle.lowercase() } // Película Z-A
            6 -> list.sortedByDescending { it.rating.toString().toFloatOrNull() ?: 0f } // Rating Max
            7 -> list.sortedBy { it.rating.toString().toFloatOrNull() ?: 0f } // Rating Min
            else -> list
        }
        _reviewsList.value = sorted
    }
}
