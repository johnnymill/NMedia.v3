package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
    val published: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorAvatar: String,
    val authorId: Long,
    val content: String,
    override val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false
) : FeedItem

data class Ad(
    override val id: Long,
    override val published: Long,
    val image: String,
) : FeedItem

data class TimingSeparator(
    override val id: Long,
    override val published: Long,
    val text: String,
) : FeedItem
