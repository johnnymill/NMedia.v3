package ru.netology.nmedia.model

data class FeedModelState(
    val acting: FeedModelActing = FeedModelActing.IDLE,
    val error: Boolean = false,
    val response: FeedResponse = FeedResponse(),
    val postId: Long = 0,
)

enum class FeedModelActing {
    IDLE,
    REMOVING,
    LIKING,
}

data class FeedResponse(
    val code: Int = 0,
    val message: String? = null
)
