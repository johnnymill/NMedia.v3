package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class FeedModelState(
    val acting: FeedModelActing = FeedModelActing.IDLE,
    val error: Boolean = false,
    val response: FeedResponse = FeedResponse(),
    val postId: Long = 0,
)

data class FeedPosts(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)

enum class FeedModelActing {
    IDLE,
    LOADING,
    REFRESHING,
    REMOVING,
    LIKING,
}

data class FeedResponse(
    val code: Int = 0,
    val message: String? = null
)
