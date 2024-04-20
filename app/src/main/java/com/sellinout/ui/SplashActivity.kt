package com.sellinout.ui

import android.os.Bundle
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.ui.login.ActivityLogin
import com.sellinout.ui.sellinout.ActivitySellInOutList
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.navigateClearStack

class SplashActivity : BaseActivity(R.layout.activity_splash) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SharePrefsKey.isGuestUser()) {
//            navigateClearStack<ActivitySellInOutList>()
            navigateClearStack<MainActivity>()
        } else {
            navigateClearStack<ActivityLogin>()
        }
    }
}