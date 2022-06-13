package com.example.fooedtra

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.fooedtra.Model.tokenModel
import com.example.fooedtra.Model.tokenPreferences
import kotlinx.coroutines.launch

class MainModel(private val pref: tokenPreferences) : ViewModel() {
    fun getUser(): LiveData<tokenModel> {
        return pref.getUser().asLiveData()
    }
    fun saveUser(token: tokenModel) {
        viewModelScope.launch {
            pref.saveToken(token)
        }
    }
}