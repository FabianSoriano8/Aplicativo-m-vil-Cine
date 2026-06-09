package com.example.appcineindie.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.example.appcineindie.databinding.FragmentVideoPlayerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VideoPlayerFragment : Fragment() {

    private var _binding: FragmentVideoPlayerBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null
    private var movieId: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoUrl = arguments?.getString("videoUrl") ?: ""
        movieId = arguments?.getString("movieId") // Recuperamos el ID

        if (videoUrl.isNotEmpty()) {
            initializePlayer(videoUrl)
        }

        binding.btnBackPlayer.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initializePlayer(videoUrl: String) {
        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            val mediaItem = MediaItem.fromUri(videoUrl)
            exoPlayer.setMediaItem(mediaItem)

            // LÓGICA DE CONTINUAR VIENDO
            val uid = auth.currentUser?.uid
            if (uid != null && movieId != null) {
                db.collection("users").document(uid)
                    .collection("progress").document(movieId!!)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val position = document.getLong("position") ?: 0L
                            exoPlayer.seekTo(position) // Salta al segundo guardado
                        }
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    }
                    .addOnFailureListener {
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    }
            } else {
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }
    }

    // Guarda el progreso en Firestore
    private fun saveProgress() {
        val uid = auth.currentUser?.uid
        val currentPosition = player?.currentPosition ?: 0L
        val duration = player?.duration ?: 0L

        // Solo guardamos si hay un usuario, una película y el video ha avanzado
        if (uid != null && movieId != null && currentPosition > 0) {
            val data = hashMapOf(
                "position" to currentPosition,
                "duration" to duration, // Guardamos la duración real detectada por el reproductor
                "timestamp" to System.currentTimeMillis()
            )
            Log.d("VideoPlayer", "Guardando progreso: $currentPosition / $duration")
            db.collection("users").document(uid)
                .collection("progress").document(movieId!!)
                .set(data)
        }
    }

    override fun onStop() {
        super.onStop()
        saveProgress() // Guardamos al pausar o salir de la app
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}