package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.FeedModelActing
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.FeedResponse
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

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

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {
    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = appAuth.authStateFlow
        .flatMapLatest { (id, _) ->
            repository.data
                .map { posts ->
                    posts.map { post -> post.copy(ownedByMe = post.authorId == id) }
                }
        }.flowOn(Dispatchers.Default)
    private val _photoState = MutableLiveData<PhotoModel?>()
    val photoState: LiveData<PhotoModel?>
        get() = _photoState
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

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
