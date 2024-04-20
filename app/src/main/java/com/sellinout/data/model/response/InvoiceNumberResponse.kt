package com.sellinout.data.model.response

import com.google.gson.annotations.SerializedName
import com.sellinout.data.model.BaseResponse

class InvoiceNumberResponse : BaseResponse() {
    @SerializedName("Data")
    val Data: String? = null
}