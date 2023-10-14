package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.CardTimingSeparatorBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimingSeparator
import java.text.DateFormat
import java.util.Date

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onViewImage(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is TimingSeparator -> R.layout.card_timing_separator
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("Unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_timing_separator -> {
                val binding = CardTimingSeparatorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TimingSeparatorViewHolder(binding)
            }
            R.layout.card_ad -> {
                val binding = CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            R.layout.card_post -> {
                val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            else -> error("Unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TimingSeparator -> (holder as? TimingSeparatorViewHolder)?.bind(item)
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("Unknown item type")
        }
    }
}

class TimingSeparatorViewHolder(
    private val binding: CardTimingSeparatorBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(timingSeparator: TimingSeparator) {
        binding.timingSeparator.text = timingSeparator.text
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        Glide.with(binding.root)
            .load("${BuildConfig.NMEDIA_SERVER}/media/${ad.image}")
            .placeholder(R.drawable.ic_image_loading_100dp)
            .error(R.drawable.ic_image_load_error_100dp)
            .timeout(10_000)
            .into(binding.image)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        Glide.with(binding.root)
            .load("${BuildConfig.NMEDIA_SERVER}/avatars/${post.authorAvatar}")
            .placeholder(R.drawable.ic_avatar_loading_100dp)
            .error(R.drawable.ic_avatar_load_error_100dp)
            .timeout(10_000)
            .circleCrop()
            .into(binding.avatar)

        if (post.attachment != null) {
            if (post.attachment.type != AttachmentType.IMAGE) {
                throw Exception("Got unsupported attachment")
            }
            Glide.with(binding.root)
                .load("${BuildConfig.NMEDIA_SERVER}/media/${post.attachment.url}")
                .placeholder(R.drawable.ic_avatar_loading_100dp)
                .error(R.drawable.ic_avatar_load_error_100dp)
                .timeout(10_000)
                .into(binding.attachment)
        }

        binding.apply {
            author.text = post.author
            published.text = DateFormat.getDateTimeInstance().format(Date(post.published * 1000))
            content.text = post.content
            // в адаптере
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"
            menu.isVisible = post.ownedByMe

            attachment.visibility = when (post.attachment) {
                null -> View.GONE
                else -> {
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        attachment.tooltipText = post.attachment.description
                    }
                    View.VISIBLE
                }
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            attachment.setOnClickListener {
                onInteractionListener.onViewImage(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
