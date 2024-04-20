package com.sellinout.data.model.request

import com.google.gson.annotations.SerializedName

data class ProductInvoiceRequest(
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
    var Quantity: Double? = null,
    @SerializedName("Price")
    var Price: Double? = null,
    @SerializedName("Discount")
    var Discount: Double? = null,
    @SerializedName("Amount")
    val Amount: Double? = null,
    @SerializedName("VchType")
    val VchType: Int? = null,
    @SerializedName("ItemType")
    val ItemType: Int? = null,
    @SerializedName("ItemImage")
    val ItemImage: String? = null,
    var discountPercent: Double? = null,
)