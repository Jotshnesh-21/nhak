package com.sellinout.ui.productscan

import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.data.model.request.ProductInvoiceRequest
import com.sellinout.databinding.ActionToolbarBinding
import com.sellinout.databinding.ActivityItemDetailBinding
import com.sellinout.utils.Const
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.amountCalculation
import com.sellinout.utils.decodeBaseImage
import com.sellinout.utils.discountAmountCalculation
import com.sellinout.utils.getCurrentDate
import com.sellinout.utils.showToast
import com.sellinout.utils.toJson

class ActivityAddItemCamera : BaseActivity(R.layout.activity_item_detail) {
    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var bindingToolbar: ActionToolbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        bindingToolbar = binding.mainToolbar
        setContentView(binding.root)
        setOnClickListener()
    }

    private var listOfProduct: ArrayList<ProductInvoiceRequest> = arrayListOf()

    private fun setOnClickListener() {
        listOfProduct = ArrayList()
        if (Prefs.getString(SharePrefsKey.CART_DATA, "") != "") {
            listOfProduct = Gson().fromJson(
                Prefs.getString(
                    SharePrefsKey.CART_DATA, ""
                ), object : TypeToken<ArrayList<ProductInvoiceRequest>>() {}.type
            )
        }
//        val filePath = intent.getStringExtra(Const.FILE_URL)
        val filePath = Const.imageBase
        val bitmap = decodeBaseImage(filePath.toString())
//        val file = File(filePath.toString())
//        if (file.exists()) {
        Glide.with(this).load(bitmap).into(binding.imgProductImage)
//        } else {
//            this.showToast("NO Exist")
//        }
        bindingToolbar.imgBackButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
        binding.btnAddToCart.setOnClickListener {
            if (binding.edtItemAmount.text.toString() == "") {
                this.showToast("Please enter amount")
            } else if (binding.edtItemDiscount.text.toString() == "") {
                this.showToast("Please enter discount")
            } else {
                val currentDate = getCurrentDate("dd-MM-yyyy")

                /* if (Const.voucherNumber == null && Const.voucherNumber == "") {
                     Const.voucherNumber =
                         (SharePrefsKey.getUserData().PreFix
                             ?: "") + randomNumberGenerate().toString()
                 }*/
                Log.e("VoucherNum", ">> ${Const.voucherNumber}")

                val amnt =
                    amountCalculation(1.0,  binding.edtItemAmount.text.toString().toDouble(),  binding.edtItemDiscount.text.toString().toDouble())
                val discount = discountAmountCalculation(
                    1.0, binding.edtItemAmount.text.toString().toDouble(),  binding.edtItemDiscount.text.toString().toDouble()
                )

                val model = ProductInvoiceRequest(
                    Prefs.getInt(SharePrefsKey.ACCOUNT_CODE, 0),
                    Const.voucherNumber.toString(),
                    currentDate,
                    Const.ITEM_NAME,
                    Const.ITEM_COLOR,
                    Const.ITEM_SIZE,
                    "",//Const.UNIT,
                    1.0,//1.0,
                    binding.edtItemAmount.text.toString().toDouble(),
                    discount/*binding.edtItemDiscount.text.toString()
                        .toDouble()*/,//Previously: 0.0  // list[4].toDouble(), //DISCOUNT
                    amnt/*binding.edtItemAmount.text.toString().toDouble()*/,
                    Const.SALE_IN_OUT.toInt(),
                    Const.ITEM_TYPE_IMAGE,
                    filePath,
                    binding.edtItemDiscount.text.toString().toDouble()//DISCOUNT Percent
                )
                listOfProduct.add(model)
                Prefs.putString(SharePrefsKey.CART_DATA, listOfProduct.toJson())
                onBackPressedDispatcher.onBackPressed()
                finish()
            }
        }
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
    }
}