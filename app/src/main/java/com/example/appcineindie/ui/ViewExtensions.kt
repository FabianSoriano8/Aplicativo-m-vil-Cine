package com.example.appcineindie.ui

import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.appcineindie.R

/**
 * Evita que un botón sea presionado varias veces seguidas rápidamente (debouncing).
 */
fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    var lastClickTime: Long = 0
    val defaultInterval = 1000
    
    this.setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < defaultInterval) {
            return@setOnClickListener
        }
        lastClickTime = SystemClock.elapsedRealtime()
        onSafeClick(it)
    }
}

/**
 * Configura múltiples vistas con un Safe Click simultáneamente.
 */
fun setSafeClick(vararg views: View, action: (View) -> Unit) {
    views.forEach { it.setSafeOnClickListener(action) }
}

/**
 * Muestra un diálogo de confirmación estándar.
 */
fun Fragment.showConfirmDialog(
    title: String,
    message: String,
    positiveButtonText: String = "Eliminar",
    onConfirm: () -> Unit
) {
    AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { _, _ -> onConfirm() }
        .setNegativeButton("Cancelar", null)
        .show()
}
