package com.sellinout.data.model.request

import com.google.gson.annotations.SerializedName

data class SettInOutDetailRequest(
    @SerializedName("AccountCode")
    val AccountCode: Int?,
    @SerializedName("VoucherNo")
    val VoucherNo: String?
)