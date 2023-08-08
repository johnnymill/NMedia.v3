package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.AuthModel
import java.io.File

interface PostRepository {
    val posts: Flow<List<Post>>

    suspend fun getAll()
    fun getNewerCount(): Flow<Int>
    suspend fun showNewer()
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(file: File, post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun signIn(login: String, password: String): AuthModel
    suspend fun signUp(login: String, password: String, name: String): AuthModel
}
