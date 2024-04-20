package com.sellinout.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class BaseResponse : Serializable {
    @SerializedName("ErrorMessage")
    var ErrorMessage: String? = null

    @SerializedName("Ststus")
    var Ststus: Int? = null
}
