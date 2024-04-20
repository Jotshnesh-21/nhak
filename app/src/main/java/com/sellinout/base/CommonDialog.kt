package com.sellinout.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.button.MaterialButton
import com.sellinout.R
import com.sellinout.utils.gone
import com.sellinout.utils.visible


class CommonDialog(private val context: Context) : BaseDialogHelper() {
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dialog_common, null)
    }
    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)

    private val dialogTitle: TextView by lazy {
        dialogView.findViewById<TextView>(R.id.dialog_title)
    }

    private val dialogDescription: TextView by lazy {
        dialogView.findViewById<TextView>(R.id.dialog_title)
    }

    private val dialogConfirm: AppCompatButton by lazy {
        dialogView.findViewById<AppCompatButton>(R.id.sign_out_dialog_yes)
    }

    private val dialogCancel: AppCompatButton by lazy {
        dialogView.findViewById<AppCompatButton>(R.id.no)
    }



    fun dismissDialog() {
        dialog?.dismiss()
    }

    fun setMessage(message: String?) {
        dialogDescription.text = message
    }


    fun setTitleValue(titleMessage: String?) {
        dialogTitle.text = titleMessage?.capitalize()
    }


    fun setConfirmButtonText(buttonText: String?) {
        dialogConfirm.text = buttonText
    }

    fun setCancelButtonText(buttonText: String?) {
        dialogCancel.text = buttonText
        dialogCancel.visible()
    }
    fun hideCancelButton() {
        dialogCancel.gone()
    }

    fun confirmClick(func: (() -> Unit)? = null) =
        with(dialogConfirm) {
            setClickListener(func)
        }

    fun cancelClick(func: (() -> Unit)? = null) =
        with(dialogCancel) {
            setClickListener(func)
        }

    private fun View.setClickListener(func: (() -> Unit)?) =
        setOnClickListener {
            func?.invoke()
        }
}
