package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAuthSigninBinding
import ru.netology.nmedia.model.AuthModelActing
import ru.netology.nmedia.viewmodel.SignInViewModel

class AuthSignInFragment : Fragment() {
    private var _binding: FragmentAuthSigninBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding: FragmentAuthSigninBinding
        get() = _binding!!

    private val viewModel: SignInViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthSigninBinding.inflate(inflater, container, false)

        binding.singin.setOnClickListener {
            val login = binding.login.text.toString()
            val password = binding.password.text.toString()
            if (login.isBlank() || password.isBlank()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.auth_empty_credentials),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.singIn(login, password)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.acting == AuthModelActing.SIGN_IN

            if (state.error) {
                val message = if (state.response.code == 0) {
                    getString(R.string.error_loading)
                } else {
                    getString(
                        R.string.error_response,
                        state.response.message.toString(),
                        state.response.code
                    )
                }
                Snackbar.make(
                    binding.root,
                    message,
                    Snackbar.LENGTH_LONG
                ).show()
            }

            if (state.acting == AuthModelActing.COMPLETE) {
                findNavController().navigateUp()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
