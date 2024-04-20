package com.sellinout.data.model.response

import com.google.gson.annotations.SerializedName
import com.sellinout.data.model.BaseResponse

class SellInOutResponse : BaseResponse() {
    @SerializedName("Data")
    val data: SellDataModel? = null
}

data class SellDataModel(
    @SerializedName("OpeningBalance")
    val openingBalance: Double? = null,
    @SerializedName("SaleInSaleOut")
    val saleInSaleOut: ArrayList<SellInOutItemModel>? = arrayListOf()
)

data class SellInOutItemModel(
    @SerializedName("VoucherNo")
    val VoucherNo: String? = null,
    @SerializedName("Date")
    val valDate: String? = null,
    @SerializedName("VchType")
    val vchType: String? = null,
    @SerializedName("Unit")
    val unit: String? = null,
    @SerializedName("Quantity")
    val quantity: Double? = null,
    @SerializedName("Amount")
    val amount: Double? = null,
)