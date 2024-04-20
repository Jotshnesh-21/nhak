package com.sellinout.data.model.response

import com.google.gson.annotations.SerializedName
import com.sellinout.data.model.BaseResponse

class LoginResponse : BaseResponse() {
    @SerializedName("Data")
    val Data: LoginResponseData? = null
}

data class LoginResponseData(
    @SerializedName("AccountMasterName") val AccountMasterName: String? = null,
    @SerializedName("Address") val Address: String? = null,
    @SerializedName("PrintName") val PrintName: String? = null,
    @SerializedName("AccountCode") val AccountCode: Int? = null,
    @SerializedName("UserName") val UserName: String? = null,
    @SerializedName("UserPassword") val UserPassword: String? = null,
    @SerializedName("CreatedDate") val CreatedDate: String? = null,
    @SerializedName("CreatedBy") val CreatedBy: Int? = null,
    @SerializedName("ModifiedDate") val ModifiedDate: String? = null,
    @SerializedName("ModifiedBy") val ModifiedBy: Int? = null,
    @SerializedName("IsActive") val IsActive: Boolean? = null,
    @SerializedName("AccountGroupId") val AccountGroupId: Int? = null,
    @SerializedName("PreFix") val PreFix: String? = null
)