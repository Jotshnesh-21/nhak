package com.sellinout.utils

import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.data.model.response.LoginResponseData

object SharePrefsKey {

    const val IS_GUEST_USER = "GUEST_USER"
    const val ACCOUNT_CODE = "ACCOUNT_CODE"
    const val CART_DATA = "CART_DATA"
    const val USER_DATA = "USER_DATA"
    const val STR_BASE_URL = "STR_BASE_URL"

    fun isGuestUser() = Prefs.getBoolean(IS_GUEST_USER, true)

    fun getUserData(): LoginResponseData =
        Gson().fromJson(Prefs.getString(USER_DATA, ""), LoginResponseData::class.java)
}