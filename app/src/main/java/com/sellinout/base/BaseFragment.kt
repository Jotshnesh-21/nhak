package com.sellinout.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
    inline fun commonDialog(func: CommonDialog.() -> Unit): AlertDialog =
        CommonDialog(requireActivity()).apply {
            func()
        }.create()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {

        super.onResume()
    }
}