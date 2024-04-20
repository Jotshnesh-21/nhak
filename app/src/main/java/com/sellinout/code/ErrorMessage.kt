package com.sellinout.code


import com.google.gson.annotations.SerializedName

data class ErrorMessage(
    @SerializedName("json")
    val json: Json,
    @SerializedName("message")
    val message: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("status")
    val status: Int
)