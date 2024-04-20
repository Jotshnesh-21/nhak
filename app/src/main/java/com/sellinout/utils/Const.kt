package com.sellinout.utils

import android.net.Uri

object Const {

    val ITEM_NAME: String = "Default"
    val ITEM_COLOR: String = "Default"
    val ITEM_SIZE: String = "Default"

    const val VCH_TYPE: String = "VCH_TYPE"
    const val VOUCHER_NUMBER: String = "VOUCHER_NUMBER"
    const val FILE_URL: String = "FILE_URL"
    var SALE_IN_OUT: String = ""
    const val IN: String = "In"
    const val OUT: String = "Out"
    const val CURRENCY_UNIT: String = "Rs."

    var voucherNumber: String = ""
    var vFilename: String = ""
    var imageUri: Uri? = null
    var imageBase: String? = null
//    var UNIT : String = ""
    //ADD PRODUCT TYPE FOR QR CODE = 1 and Image = 2
    const val ITEM_TYPE_QR_CODE = 1
    const val ITEM_TYPE_IMAGE = 2
}