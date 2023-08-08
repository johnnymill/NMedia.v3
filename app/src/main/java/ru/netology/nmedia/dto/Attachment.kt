package ru.netology.nmedia.dto

data class Attachment(
    val type: AttachmentType,
    val url: String,
    val description: String
)

enum class AttachmentType {
    IMAGE,
}
