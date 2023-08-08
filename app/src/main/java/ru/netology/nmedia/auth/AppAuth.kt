package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.model.AuthModel

class AppAuth private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authStateFlow: MutableStateFlow<AuthModel>

    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            prefs.edit { clear() }
            _authStateFlow = MutableStateFlow(AuthModel())
        } else {
            _authStateFlow = MutableStateFlow(AuthModel(id, token))
        }
        uploadPushToken()
    }

    val authStateFlow = _authStateFlow.asStateFlow()
    var pushToken: PushToken? = null

    fun uploadPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pt = PushToken(token ?: Firebase.messaging.token.await())
                Api.retrofitService.uploadPushToken(pt)
                pushToken = pt
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setUser(user: AuthModel) {
        _authStateFlow.value = user
        prefs.edit {
            putLong(ID_KEY, user.id)
            putString(TOKEN_KEY, user.token)
        }
        uploadPushToken()
    }

    fun removeUser() {
        _authStateFlow.value = AuthModel()
        prefs.edit { clear() }
        uploadPushToken()
    }

    fun isUserValid() = authStateFlow.value != AuthModel()

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"

        @Volatile
        private var instance: AppAuth? = null

        @Synchronized
        fun initAppAuth(context: Context): AppAuth {
            return instance ?: AppAuth(context).apply { instance = this }
        }

        fun getInstance(): AppAuth = requireNotNull(instance) { "initAppAuth was not invoked" }
    }
}
