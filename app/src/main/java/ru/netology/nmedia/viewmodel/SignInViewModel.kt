package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.AuthModelActing
import ru.netology.nmedia.model.AuthModelState
import ru.netology.nmedia.model.AuthResponse
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {
    private val _state = SingleLiveEvent<AuthModelState>()
    val state: LiveData<AuthModelState>
        get() = _state

    fun singIn(login: String, password: String) = viewModelScope.launch {
        try {
            _state.value = AuthModelState(acting = AuthModelActing.SIGN_IN)
            val response = repository.signIn(login, password)
            response.token?.let { appAuth.setUser(response) }
            _state.postValue(AuthModelState(acting = AuthModelActing.COMPLETE))
        } catch (e: Exception) {
            val resp = if (e is ApiError) AuthResponse(e.status, e.code) else AuthResponse()
            _state.postValue(
                AuthModelState(error = true, response = resp)
            )
        }
    }
}
