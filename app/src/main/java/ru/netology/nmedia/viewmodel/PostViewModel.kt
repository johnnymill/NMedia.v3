package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.FeedModelActing
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.FeedPosts
import ru.netology.nmedia.model.FeedResponse
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    authorId = 0,
    likedByMe = false,
    likes = 0,
    published = "",
    attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )
    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    @OptIn(ExperimentalCoroutinesApi::class)
    val data: LiveData<FeedPosts> = AppAuth.getInstance().authStateFlow.flatMapLatest { (id, _) ->
        repository.posts
            .map { posts ->
                posts.map { post -> post.copy(ownedByMe = post.authorId == id) }
            }
            .map(::FeedPosts)
    }.asLiveData(Dispatchers.Default)
    private val _photoState = MutableLiveData<PhotoModel?>()
    val photoState: LiveData<PhotoModel?>
        get() = _photoState
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewerCount()
            .asLiveData(Dispatchers.Default)
    }

    init {
        loadPosts()
    }

    private fun reload(isInitial: Boolean) = viewModelScope.launch {
        val acting = if (isInitial) FeedModelActing.LOADING else FeedModelActing.REFRESHING
        try {
            _state.value = FeedModelState(acting = acting)
            repository.getAll()
            _state.postValue(FeedModelState())
        } catch (e: Exception) {
            val resp = if (e is ApiError) FeedResponse(e.status, e.code) else FeedResponse()
            _state.postValue(
                FeedModelState(acting = acting, error = true, response = resp)
            )
        }
    }

    fun loadPosts() {
        reload(true)
    }

    fun refreshPosts() {
        reload(false)
    }

    fun loadNewPosts() = viewModelScope.launch {
        repository.showNewer()
    }

    fun save() {
        edited.value?.let { post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    _photoState.value?.let { photoModel ->
                        repository.saveWithAttachment(photoModel.file, post)
                    } ?: repository.save(post)
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    val resp = if (e is ApiError) FeedResponse(e.status, e.code) else FeedResponse()
                    _state.postValue(
                        FeedModelState(error = true, response = resp)
                    )
                }
            }
        }
        _photoState.value = null
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(photoModel: PhotoModel?) {
        _photoState.value = photoModel
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            _state.value = FeedModelState(acting = FeedModelActing.LIKING)
            repository.likeById(id)
            _state.postValue(FeedModelState())
        } catch (e: Exception) {
            val resp = if (e is ApiError) FeedResponse(e.status, e.code) else FeedResponse()
            _state.postValue(
                FeedModelState(
                    acting = FeedModelActing.LIKING,
                    error = true,
                    response = resp,
                    postId = id
                )
            )
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            _state.value = FeedModelState(acting = FeedModelActing.REMOVING)
            repository.removeById(id)
            _state.postValue(FeedModelState())
        } catch (e: Exception) {
            val resp = if (e is ApiError) FeedResponse(e.status, e.code) else FeedResponse()
            _state.postValue(
                FeedModelState(
                    acting = FeedModelActing.REMOVING,
                    error = true,
                    response = resp,
                    postId = id
                )
            )
        }
    }
}
