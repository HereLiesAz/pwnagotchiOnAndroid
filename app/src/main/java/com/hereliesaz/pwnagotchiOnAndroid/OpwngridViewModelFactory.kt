package com.hereliesaz.pwnagotchiOnAndroid

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OpwngridViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OpwngridViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OpwngridViewModel(OpwngridClient()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
