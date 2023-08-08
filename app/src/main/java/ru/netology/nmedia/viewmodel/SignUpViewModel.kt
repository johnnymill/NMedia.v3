package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.AuthModelActing
import ru.netology.nmedia.model.AuthModelState
import ru.netology.nmedia.model.AuthResponse
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

class SignUpViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    private val _state = SingleLiveEvent<AuthModelState>()
    val state: LiveData<AuthModelState>
        get() = _state

    fun singUp(login: String, password: String, name: String) = viewModelScope.launch {
        try {
            _state.value = AuthModelState(acting = AuthModelActing.SIGN_UP)
            val response = repository.signUp(login, password, name)
            response.token?.let { AppAuth.getInstance().setUser(response) }
            _state.postValue(AuthModelState(acting = AuthModelActing.COMPLETE))
        } catch (e: Exception) {
            val resp = if (e is ApiError) AuthResponse(e.status, e.code) else AuthResponse()
            _state.postValue(
                AuthModelState(error = true, response = resp)
            )
        }
    }
}
