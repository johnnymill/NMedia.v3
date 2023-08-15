package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentImageBinding
import ru.netology.nmedia.util.StringArg

@AndroidEntryPoint
class ImageFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageBinding.inflate(
            inflater,
            container,
            false
        )

        val url = arguments?.textArg
            ?: throw NullPointerException("Oops! Image URL is undefined")

        Glide.with(binding.root)
            .load("${BuildConfig.NMEDIA_SERVER}/media/$url")
            .placeholder(R.drawable.ic_image_loading_100dp)
            .error(R.drawable.ic_image_load_error_100dp)
            .timeout(10_000)
            .into(binding.image)

        return binding.root
    }
}
