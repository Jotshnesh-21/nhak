package com.sellinout.base

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.ui.login.ActivityLogin
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.navigateClearStack

abstract class BaseActivity(
    @LayoutRes resId: Int
) : AppCompatActivity(resId) {

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context)

    }

  /*  override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        this.changeStatusColor(this,R.color.white)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onRestart() {
        super.onRestart()
    }

    private var progressDialogue: Dialog? = null
    fun requestDidStart() {
        if (progressDialogue != null) {
            if (progressDialogue!!.isShowing) {
            }
        } else {
            try {
                progressDialogue = ProgressDialog(this)
                progressDialogue!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun requestDidFinish() {
        if (progressDialogue != null) {
            if (progressDialogue!!.isShowing) {
                try {

                    progressDialogue!!.dismiss()
                    progressDialogue = null

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    var alertDialog: Dialog? = null
    fun showLogoutDlg(onOkay: () -> Unit) {
        /*  alertDialog = Dialog(this)
          alertDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
          alertDialog?.setContentView(R.layout.common_yes_no_dialog)
          alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
          alertDialog?.setCancelable(true)

          alertDialog?.findViewById<View>(R.id.dialogYes)?.setOnClickListener {
              onOkay.invoke()
              alertDialog?.dismiss()
          }
          alertDialog?.findViewById<View>(R.id.dialogCancel)?.setOnClickListener {
              alertDialog?.dismiss()
          }
          alertDialog?.show()*/
        commonDialog {
            setTitleValue("Are you sure want to Logout?")
            setConfirmButtonText("Yes")
            setCancelButtonText("No")
            confirmClick {
                dismissDialog()
                Prefs.putBoolean(SharePrefsKey.IS_GUEST_USER, true)
                Prefs.clear()
                navigateClearStack<ActivityLogin>()
            }
            cancelClick {
                dismissDialog()
            }
        }.show()
    }

    inline fun commonDialog(func: CommonDialog.() -> Unit): AlertDialog =
        CommonDialog(this).apply {
            func()
        }.create()
}