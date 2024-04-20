package com.sellinout.ui.printscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.R
import com.sellinout.base.BaseFragment
import com.sellinout.data.model.request.ProductInvoiceRequest
import com.sellinout.databinding.ActivityCartPrintSummaryBinding
import com.sellinout.ui.cartsummary.adapter.AdapterCartSummary
import com.sellinout.utils.SharePrefsKey

class PrintScreenActivity : BaseFragment(R.layout.activity_cart_print_summary) {
    private lateinit var binding: ActivityCartPrintSummaryBinding
    private var arrayList: ArrayList<ProductInvoiceRequest> = arrayListOf()
    private var adapterPrintScreen: AdapterCartSummary? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityCartPrintSummaryBinding.inflate(layoutInflater)
        val view = binding.root
        initView()
        setOnClickListener()
        return view
    }

    private fun initView() {
        arrayList = Gson().fromJson(
            Prefs.getString(
                SharePrefsKey.CART_DATA,
                ""
            ), object : TypeToken<ArrayList<ProductInvoiceRequest>>() {}.type
        )
        setSellInOutAdapter()
    }

    fun newInstance(): PrintScreenActivity {
        val args = Bundle()

        val fragment = PrintScreenActivity()
        fragment.arguments = args
        return fragment
    }

    private fun setOnClickListener() {
        binding.btnPrint.setOnClickListener {

        }
    }

    private fun setSellInOutAdapter() {
        totalAmountCalculation()
        binding.rvListOfStock.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapterPrintScreen =
            AdapterCartSummary(false, requireActivity(), arrayList, itemDelete = { item, position ->
            }, itemEdit = { item, position ->

            })
        binding.rvListOfStock.adapter = adapterPrintScreen
    }

    var itemAmount: Double? = 0.0
    var itemDiscount: Double? = 0.0
    var itemTotalAmount: Double? = 0.0
    private fun totalAmountCalculation() {
        itemAmount = 0.0
        itemDiscount = 0.0
        itemTotalAmount = 0.0
        arrayList.forEach {
            itemAmount = itemAmount?.plus(it.Price!!)
            itemDiscount = itemDiscount?.plus(it.Discount!!)
        }
        itemTotalAmount = itemAmount?.minus(itemDiscount!!)

        binding.txtItemAmountValue.text =
            "Rs. " + String.format("%.2f", itemAmount).toDouble().toString()
        binding.txtDiscountValue.text =
            "Rs. " + String.format("%.2f", itemDiscount).toDouble().toString()
        binding.txtTotalAmountValue.text =
            "Rs. " + String.format("%.2f", itemTotalAmount).toDouble().toString()
    }
}