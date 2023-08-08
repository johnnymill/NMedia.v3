package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel: ViewModel() {
    val authLiveData = AppAuth.getInstance().authStateFlow.asLiveData(Dispatchers.Default)
    val isAuthorized: Boolean
        get() = AppAuth.getInstance().authStateFlow.value.token != null
}
