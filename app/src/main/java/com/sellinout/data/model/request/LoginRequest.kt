package com.sellinout.data.model.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("UserName")
    val UserName: String?,
    @SerializedName("Password")
    val Password: String?
)