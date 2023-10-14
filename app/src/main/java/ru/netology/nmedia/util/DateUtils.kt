package ru.netology.nmedia.util

object DateUtils {
    fun ago(seconds: Long): String =
        // TODO Don't forget about locale
        when {
            seconds <= 24 * 60 * 60 -> "Сегодня"
            seconds <= 48 * 60 * 60 -> "Вчера"
            else -> "На прошлой неделе"
        }
}
