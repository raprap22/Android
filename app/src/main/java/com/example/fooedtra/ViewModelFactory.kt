package com.example.fooedtra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fooedtra.Model.tokenPreferences

class ViewModelFactory(private val pref: tokenPreferences) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainModel::class.java) -> {
                MainModel(pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}