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
import ru.netology.nmedia.databinding.FragmentAuthSignupBinding
import ru.netology.nmedia.model.AuthModelActing
import ru.netology.nmedia.viewmodel.SignUpViewModel

class AuthSignUpFragment : Fragment() {
    private var _binding: FragmentAuthSignupBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding: FragmentAuthSignupBinding
        get() = _binding!!

    private val viewModel: SignUpViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthSignupBinding.inflate(inflater, container, false)

        binding.singUp.setOnClickListener {
            val name = binding.name.text.toString()
            val login = binding.login.text.toString()
            val password = binding.password.text.toString()
            val passwordRepeat = binding.passwordRepeat.text.toString()
            val error = when {
                login.isBlank() || password.isBlank() -> getString(R.string.auth_empty_credentials)
                passwordRepeat != password -> getString(R.string.auth_passwords_mismatch)
                else -> ""
            }
            if (error.isNotBlank()) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.singUp(login, password, name)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.acting == AuthModelActing.SIGN_UP

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
