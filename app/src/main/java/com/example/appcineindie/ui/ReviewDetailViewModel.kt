package com.example.appcineindie.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appcineindie.data.Reply
import com.example.appcineindie.data.Review
import com.example.appcineindie.data.repository.CineRepository
import com.google.firebase.firestore.ListenerRegistration

class ReviewDetailViewModel : ViewModel() {

    private val repository = CineRepository()
    private var reviewListener: ListenerRegistration? = null

    private val _review = MutableLiveData<Review?>()
    val review: LiveData<Review?> get() = _review

    fun listenForReview(reviewId: String) {
        reviewListener?.remove()
        reviewListener = repository.listenToReviewById(reviewId) { review ->
            _review.value = review
        }
    }

    fun addReply(reviewId: String, reply: Reply, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.addReply(reviewId, reply, onSuccess, onFailure)
    }

    fun deleteReply(reviewId: String, reply: Reply, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.deleteReply(reviewId, reply, onSuccess, onFailure)
    }

    override fun onCleared() {
        super.onCleared()
        reviewListener?.remove()
    }
}
