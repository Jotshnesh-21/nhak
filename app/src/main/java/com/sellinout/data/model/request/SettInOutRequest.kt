package com.sellinout.data.model.request

import com.google.gson.annotations.SerializedName

data class SettInOutRequest(
    @SerializedName("AccountCode")
    val AccountCode: Int?
)