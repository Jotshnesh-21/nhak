package com.sellinout.ui.cartsummary

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager.LayoutParams
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.BuildConfig
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.base.BaseFragment
import com.sellinout.data.model.BaseResponse
import com.sellinout.data.model.request.ProductInvoiceRequest
import com.sellinout.databinding.CartSummaryActivityBinding
import com.sellinout.network.ApiService
import com.sellinout.network.RetrofitClient
import com.sellinout.ui.MainActivity
import com.sellinout.ui.cartsummary.adapter.AdapterCartSummary
import com.sellinout.ui.printscreen.BottomSheetPrinterList
import com.sellinout.ui.productscan.ActivityProductScan
import com.sellinout.ui.productscan.ProductScanViewModel
import com.sellinout.utils.Const
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.checkBluetoothConnectPermissions
import com.sellinout.utils.checkBluetoothPermissions
import com.sellinout.utils.getCurrentDate
import com.sellinout.utils.gone
import com.sellinout.utils.handleResponse
import com.sellinout.utils.invisible
import com.sellinout.utils.navigateClearStack
import com.sellinout.utils.requestBluetoothConnectPermissions
import com.sellinout.utils.requestBluetoothPermissions
import com.sellinout.utils.showToast
import com.sellinout.utils.toJson
import com.sellinout.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class CardSummaryActivity : BaseFragment(R.layout.cart_summary_activity) {
    private val addToCartViewModel by viewModels<ProductScanViewModel>()
    private lateinit var binding: CartSummaryActivityBinding
    private var adapterSummary: AdapterCartSummary? = null
    private var arrayList: ArrayList<ProductInvoiceRequest> = arrayListOf()
    private var arrayListOrg: ArrayList<ProductInvoiceRequest> = arrayListOf()
    var itemAmount: Double? = 0.0
    var itemDiscount: Double? = 0.0
    var itemTotalAmount: Double? = 0.0

    fun newInstance(): CardSummaryActivity {
        val args = Bundle()

        val fragment = CardSummaryActivity()
        fragment.arguments = args
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = CartSummaryActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setOnClickListener()
        init()
//        attachObserver()
        return view
    }

    private fun init() {
        (requireActivity() as MainActivity).bindingContainMain.imgCart.visible()
        (requireActivity() as MainActivity).bindingContainMain.txtItemCount.gone()
//        (requireActivity() as MainActivity).bindingContainMain.txtTitle.text = requireActivity().getString(R.string.str_cart)
        arrayList = Gson().fromJson(
            Prefs.getString(
                SharePrefsKey.CART_DATA, ""
            ), object : TypeToken<ArrayList<ProductInvoiceRequest>>() {}.type
        )
        arrayListOrg =  Gson().fromJson(
            Prefs.getString(
                SharePrefsKey.CART_DATA, ""
            ), object : TypeToken<ArrayList<ProductInvoiceRequest>>() {}.type
        )
        binding.txtOrderDate.text = requireActivity().getCurrentDate("MMM dd, yyyy")
        if (Const.voucherNumber != "") {
            binding.txtOrderId.text = "Order #" + Const.voucherNumber.toString() ?: ""
            binding.txtOrderId.visible()
        } else {
            binding.txtOrderId.invisible()
        }
        setSellInOutAdapter()
    }

    private fun setOnClickListener() {
        binding.btnPrint.setOnClickListener {
            printerListBottomSheet()
        }
        binding.btnCheckout.setOnClickListener {
            if (arrayList.size > 0) {
                Log.e("CARTLIST", ">> ${arrayList.toJson()}")
//                addToCartViewModel.addToCart(arrayList)
                addToCartAPICall()

            } else {
                requireActivity().showToast("Please Scan at least one product")
            }
        }
        binding.btnBack.setOnClickListener {
            (requireActivity() as MainActivity).openFragment(
                ActivityProductScan().newInstance()
            )
        }
        binding.btnAddMore.setOnClickListener {
            (requireActivity() as MainActivity).openFragment(
                ActivityProductScan().newInstance()
            )
        }
    }


private fun checkoutButtonEnableDisable(isEnable : Boolean=true){
    binding.btnCheckout.isEnabled =isEnable
    binding.btnCheckout.isClickable =isEnable
}
    private fun addToCartAPICall() {
        checkoutButtonEnableDisable(false)
        (requireActivity() as BaseActivity).requestDidStart()
        val apiService = RetrofitClient.getClient(
            Prefs.getString(
                SharePrefsKey.STR_BASE_URL,
                BuildConfig.BASE_URL
            )
        )
            .create(ApiService::class.java)
        val call = apiService.addToCart(arrayList)
        call.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                (requireActivity() as BaseActivity).requestDidFinish()
                checkoutButtonEnableDisable(true)
                if (response.isSuccessful) {
                    val it = response.body()
                    if (it?.Ststus == 1) {
                        Const.voucherNumber=""
                        requireActivity().runOnUiThread {
                            requireActivity().showToast("Item Added to the cart")
                            binding.btnCheckout.gone()
                            binding.btnPrint.visible()
                            commonDialog {
                                setTitleValue("Do you want to print the bill?")
                                setConfirmButtonText("Yes")
                                setCancelButtonText("No")
                                confirmClick {
                                    dismissDialog()
                                    //PRINT
                                    printerListBottomSheet()
                                }
                                cancelClick {
                                    dismissDialog()
                                    requireActivity().navigateClearStack<MainActivity>()
                                }
                            }.show()
                        }
                    } else {
                        requireActivity().showToast(it?.ErrorMessage)
                    }
                } else {
                    requireActivity().showToast("Something went wrong, Please try again after sometime")
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                checkoutButtonEnableDisable(true)
                (requireActivity() as BaseActivity).requestDidFinish()
                requireActivity().showToast("Something went wrong, Please try again after sometime")
            }

        })
    }

    private fun setSellInOutAdapter() {
        totalAmountCalculation()
        binding.rvListOfStock.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapterSummary =
            AdapterCartSummary(false, requireActivity(), arrayList, itemDelete = { item, position ->
                commonDialog {
                    setTitleValue("Are you sure want to delete?")
                    setConfirmButtonText("Yes")
                    setCancelButtonText("No")
                    confirmClick {
                        dismissDialog()
                        arrayList.removeAt(position)
                        arrayListOrg.removeAt(position)
                        Prefs.putString(SharePrefsKey.CART_DATA, arrayList.toJson())
                        adapterSummary?.notifyDataSetChanged()
                        totalAmountCalculation()
                        if (arrayList.size == 0) {
                            Prefs.putString(SharePrefsKey.CART_DATA, "")
                        }
                    }
                    cancelClick {
                        dismissDialog()
                    }
                }.show()
            }, itemEdit = { item, position ->
                editQuantityDialog(item, position)
            })
        binding.rvListOfStock.adapter = adapterSummary
    }

    private fun editQuantityDialog(item: ProductInvoiceRequest, position: Int) {
        val alertDialog = Dialog(requireActivity())
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.dialog_edit_quantity)
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        alertDialog.setCancelable(true)

        alertDialog.findViewById<AppCompatTextView>(R.id.txtStockName)?.text = item.ItemName
        val edtStockQuantity = alertDialog.findViewById<AppCompatEditText>(R.id.edtStockQuantity)

        alertDialog.findViewById<View>(R.id.btnDone)?.setOnClickListener {
            if (edtStockQuantity.text.toString() == "") {
                requireActivity().showToast(getString(R.string.enter_stock_quantity))
            } else if (edtStockQuantity.text.toString() == "0") {
                requireActivity().showToast(getString(R.string.enter_proper_stock_quantity))
            } else {

                val model = arrayListOrg.get(position)

                val quantity = edtStockQuantity.text.toString().toDouble()
//                val amountBeforeDisc = quantity * item.Price!!
                val amountBeforeDisc = quantity * model.Price!!
//                val discountFinal: Double = arrayList.get(position).Discount!! * quantity
                val discountFinal: Double =model.Discount!! * quantity

                arrayList.get(position).Discount = discountFinal
                arrayList.get(position).Quantity = quantity
//                arrayList.get(position).Price = amountBeforeDisc
                adapterSummary?.notifyDataSetChanged()
                totalAmountCalculation()
                alertDialog.dismiss()

            }
        }
        alertDialog.findViewById<View>(R.id.btnCancel)?.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun attachObserver() {
        handleResponse(addToCartViewModel.addToCartViewModelLiveData) {
            if (it.Ststus == 1) {
                requireActivity().runOnUiThread {
                    requireActivity().showToast("Item Added to the cart")/*  (requireActivity() as MainActivity).openFragment(
                          PrintScreenActivity().newInstance()
                      )*/
                    binding.btnCheckout.gone()
                    binding.btnPrint.visible()
                    commonDialog {
                        setTitleValue("Do you want to print the bill?")
                        setConfirmButtonText("Yes")
                        setCancelButtonText("No")
                        confirmClick {
                            dismissDialog()
                            //PRINT
                            printerListBottomSheet()
                        }
                        cancelClick {
                            dismissDialog()
                            requireActivity().navigateClearStack<MainActivity>()
                        }
                    }.show()
                }
            } else {
                requireActivity().showToast(it.ErrorMessage)
            }
        }
    }

    private fun printerListBottomSheet() {
        if (requireContext().checkBluetoothPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (requireContext().checkBluetoothConnectPermissions()) {
                    val sheet = BottomSheetPrinterList(itemClick = { position, model ->

                    })
                    sheet.show(
                        requireActivity().supportFragmentManager,
                        BottomSheetPrinterList::class.java.toString()
                    )
                } else {
                    requireActivity().requestBluetoothConnectPermissions()
                }
            } else {
                val sheet = BottomSheetPrinterList(itemClick = { position, model ->

                })
                sheet.show(
                    requireActivity().supportFragmentManager,
                    BottomSheetPrinterList::class.java.toString()
                )
            }
        } else {
            requireActivity().requestBluetoothPermissions()
        }

    }

    private fun totalAmountCalculation() {
        itemAmount = 0.0
        itemDiscount = 0.0
        itemTotalAmount = 0.0
        var totalQty = 0
        arrayList.forEach {
            itemAmount = itemAmount?.plus((it.Price!! * it.Quantity!!))
            itemDiscount = itemDiscount?.plus(it.Discount!!)
            totalQty = totalQty.plus(it.Quantity?.toInt()!!)
        }
        itemTotalAmount = itemAmount?.minus(itemDiscount!!)

//        binding.txtTotalQtyValue.text = totalQty.toString()
        binding.txtItemAmountValue.text =
            "${Const.CURRENCY_UNIT} " + String.format("%.2f", itemAmount).toDouble().toString()
        binding.txtDiscountValue.text =
            "${Const.CURRENCY_UNIT} " + String.format("%.2f", itemDiscount).toDouble().toString()
        binding.txtTotalAmountValue.text =
            "${Const.CURRENCY_UNIT} " + String.format("%.2f", itemTotalAmount).toDouble().toString()
    }

    private fun amountCalculation(qty: Double, price: Double, discountPer: Double): Double {
        val amountBeforeDisc = qty * price
        val discAmount = amountBeforeDisc * discountPer / 100
        return amountBeforeDisc - discAmount
    }

    private fun discountAmountCalculation(qty: Double, price: Double, discountPer: Double): Double {
        val amountBeforeDisc = qty * price
        return amountBeforeDisc * discountPer / 100
    }
}