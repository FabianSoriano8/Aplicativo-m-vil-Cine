package com.example.appcineindie.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object LoadingUtils {

    fun showLoading(fragmentManager: FragmentManager) {
        if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) return
        
        val existingFragment = fragmentManager.findFragmentByTag(LoadingDialogFragment.TAG)
        if (existingFragment == null) {
            try {
                fragmentManager.beginTransaction()
                    .add(LoadingDialogFragment.newInstance(), LoadingDialogFragment.TAG)
                    .commitNowAllowingStateLoss()
            } catch (e: Exception) {
                // Fallback en caso de que commitNow no sea posible
                LoadingDialogFragment.newInstance().show(fragmentManager, LoadingDialogFragment.TAG)
            }
        }
    }

    fun hideLoading(fragmentManager: FragmentManager) {
        val fragment = fragmentManager.findFragmentByTag(LoadingDialogFragment.TAG)
        if (fragment is LoadingDialogFragment) {
            fragment.dismissAllowingStateLoss()
        }
    }
}

fun Fragment.showLoading() {
    LoadingUtils.showLoading(parentFragmentManager)
}

fun Fragment.hideLoading() {
    LoadingUtils.hideLoading(parentFragmentManager)
}
