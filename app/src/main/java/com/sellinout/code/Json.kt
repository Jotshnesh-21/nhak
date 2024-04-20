package com.sellinout.code


import com.google.gson.annotations.SerializedName

data class Json(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
)