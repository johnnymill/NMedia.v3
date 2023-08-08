package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth

class AuthSignOutFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.auth_signout_confirm)
                .setPositiveButton(R.string.auth_signout_yes) { dialog, _ ->
                    AppAuth.getInstance().removeUser()
                    dialog.cancel()
                }
                .setNegativeButton(R.string.auth_signout_no) { dialog, _ ->
                    dialog.cancel()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
