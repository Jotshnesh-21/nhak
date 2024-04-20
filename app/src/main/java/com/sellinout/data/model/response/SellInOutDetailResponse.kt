package com.sellinout.data.model.response

import com.google.gson.annotations.SerializedName
import com.sellinout.data.model.BaseResponse

class SellInOutDetailResponse : BaseResponse() {
    @SerializedName("Data")
    val dataList: ArrayList<SellInOutDetailModel>? = null
}

data class SellInOutDetailModel(
    @SerializedName("AccountCode")
    val AccountCode: Int? = null,
    @SerializedName("VoucherNo")
    val VoucherNo: String? = null,
    @SerializedName("VoucherDate")
    val VoucherDate: String? = null,
    @SerializedName("ItemName")
    val ItemName: String? = null,
    @SerializedName("ItemColor")
    val ItemColor: String? = null,
    @SerializedName("ItemSize")
    val ItemSize: String? = null,
    @SerializedName("Unit")
    val Unit: String? = null,
    @SerializedName("Quantity")
    val Quantity: Double? = null,
    @SerializedName("Price")
    val Price: Double? = null,
    @SerializedName("Discount")
    val Discount: Double? = null,
    @SerializedName("Amount")
    val Amount: Double? = null,
    @SerializedName("VchType")
    val VchType: Int? = null,
    @SerializedName("ItemType")
    val ItemType: Int? = null,
    @SerializedName("ItemImage")
    val ItemImage: String? = null,
    @SerializedName("ErrorMsg")
    val ErrorMsg: String? = null
)