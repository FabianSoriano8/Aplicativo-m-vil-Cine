package com.example.appcineindie.data.repository

import com.example.appcineindie.data.Movie
import com.example.appcineindie.data.Reply
import com.example.appcineindie.data.Review
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class CineRepository {
    private val db = FirebaseFirestore.getInstance()

    // --- Movies ---

    fun fetchMovies(onSuccess: (List<Movie>) -> Unit) {
        db.collection("movies").get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.toObjects(Movie::class.java))
            }
    }

    fun listenToMovies(onUpdate: (List<Movie>) -> Unit): ListenerRegistration {
        return db.collection("movies").addSnapshotListener { snapshot, _ ->
            snapshot?.let { onUpdate(it.toObjects(Movie::class.java)) }
        }
    }

    fun getMovieById(movieId: String, onSuccess: (Movie?) -> Unit) {
        db.collection("movies").document(movieId).get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.toObject(Movie::class.java))
            }
    }

    // --- Reviews ---

    fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("reviews").add(review)
            .addOnSuccessListener {
                updateMovieAverageRating(review.movieId)
                onSuccess()
            }
            .addOnFailureListener(onFailure)
    }

    fun deleteReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("reviews").document(review.id).delete()
            .addOnSuccessListener {
                updateMovieAverageRating(review.movieId)
                onSuccess()
            }
            .addOnFailureListener(onFailure)
    }

    fun listenToReviews(movieId: String = "", onUpdate: (List<Review>) -> Unit): ListenerRegistration {
        val query = if (movieId.isEmpty()) {
            db.collection("reviews").orderBy("timestamp", Query.Direction.DESCENDING)
        } else {
            db.collection("reviews")
                .whereEqualTo("movieId", movieId)
        }

        return query.addSnapshotListener { snapshot, _ ->
            snapshot?.let { onUpdate(it.toObjects(Review::class.java)) }
        }
    }

    fun listenToReviewById(reviewId: String, onUpdate: (Review?) -> Unit): ListenerRegistration {
        return db.collection("reviews").document(reviewId)
            .addSnapshotListener { snapshot, _ ->
                onUpdate(snapshot?.toObject(Review::class.java))
            }
    }

    // --- Replies ---

    fun addReply(reviewId: String, reply: Reply, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("reviews").document(reviewId)
            .update("replies", FieldValue.arrayUnion(reply))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun deleteReply(reviewId: String, reply: Reply, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("reviews").document(reviewId)
            .update("replies", FieldValue.arrayRemove(reply))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    // --- Internal Logic ---

    private fun updateMovieAverageRating(movieId: String) {
        db.collection("reviews").whereEqualTo("movieId", movieId).get()
            .addOnSuccessListener { snapshot ->
                val reviews = snapshot.toObjects(Review::class.java)
                val formattedAverage = if (reviews.isNotEmpty()) {
                    val avg = reviews.sumOf { it.rating?.toString()?.toDoubleOrNull() ?: 0.0 } / reviews.size
                    String.format("%.1f", avg)
                } else ""
                db.collection("movies").document(movieId).update("rating", formattedAverage)
            }
    }
}
