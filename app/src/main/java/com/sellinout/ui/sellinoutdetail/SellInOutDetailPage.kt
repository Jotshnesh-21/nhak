package com.sellinout.ui.sellinoutdetail

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.BuildConfig
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.base.BaseFragment
import com.sellinout.data.model.BaseResponse
import com.sellinout.data.model.request.SettInOutDetailRequest
import com.sellinout.data.model.response.SellInOutDetailModel
import com.sellinout.data.model.response.SellInOutDetailResponse
import com.sellinout.databinding.ActivitySellinoutDetailSummaryBinding
import com.sellinout.network.ApiService
import com.sellinout.network.RetrofitClient
import com.sellinout.ui.MainActivity
import com.sellinout.ui.printscreen.BottomSheetPrinterList
import com.sellinout.ui.sellinout.SellInOutViewModel
import com.sellinout.ui.sellinoutdetail.adapter.AdapterSelInOutDetailSummary
import com.sellinout.utils.Const
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.checkBluetoothConnectPermissions
import com.sellinout.utils.checkBluetoothPermissions
import com.sellinout.utils.getCurrentDate
import com.sellinout.utils.getFormattedDate
import com.sellinout.utils.gone
import com.sellinout.utils.handleResponse
import com.sellinout.utils.requestBluetoothConnectPermissions
import com.sellinout.utils.requestBluetoothPermissions
import com.sellinout.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class SellInOutDetailPage : BaseFragment(R.layout.activity_sellinout_detail_summary) {

    private lateinit var binding: ActivitySellinoutDetailSummaryBinding
    private val sellInOutDetailViewModel by viewModels<SellInOutViewModel>()
    private var adapterDetail: AdapterSelInOutDetailSummary? = null
    var itemAmount: Double? = 0.0
    var itemDiscount: Double? = 0.0
    var itemTotalAmount: Double? = 0.0

    fun newInstance(voucherNo: String, strVchType: String): SellInOutDetailPage {
        val args = Bundle()
        args.putString(Const.VOUCHER_NUMBER, voucherNo)
        args.putString(Const.VCH_TYPE, strVchType)
        val fragment = SellInOutDetailPage()
        fragment.arguments = args
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = ActivitySellinoutDetailSummaryBinding.inflate(inflater, container, false)
        val view = binding.root
//        attachObserver()
        init()
        setOnClickListener()
        return view
    }

    private fun init() {
        (requireActivity() as MainActivity).bindingContainMain.imgCart.gone()
        (requireActivity() as MainActivity).bindingContainMain.txtItemCount.gone()
        if (arguments?.getString(Const.VCH_TYPE) == "1") { //SELL OUT == 1 CODE
            (requireActivity() as MainActivity).bindingContainMain.txtTitle.text =
                requireActivity().getString(R.string.str_sale_out)
            binding.txtOrderSummaryLabel.text =
                requireActivity().getString(R.string.str_sell_out_summary)
        } else { // SELL IN == 2 CODE
            (requireActivity() as MainActivity).bindingContainMain.txtTitle.text =
                requireActivity().getString(R.string.str_sale_in)
            binding.txtOrderSummaryLabel.text =
                requireActivity().getString(R.string.str_sell_in_summary)
        }
        /* sellInOutDetailViewModel.getInvoiceDetail(
             SettInOutDetailRequest(
                 Prefs.getInt(
                     SharePrefsKey.ACCOUNT_CODE, 0
                 ), arguments?.getString(Const.VOUCHER_NUMBER, "")
             )
         )*/
        getInvoiceDetail()
        binding.txtOrderDate.text = requireActivity().getCurrentDate("MMM dd, yyyy")

    }

    private fun getInvoiceDetail() {
        (requireActivity() as BaseActivity).requestDidStart()
        val apiService = RetrofitClient.getClient(
            Prefs.getString(
                SharePrefsKey.STR_BASE_URL,
                BuildConfig.BASE_URL
            )
        ).create(ApiService::class.java)
        val call = apiService.getInvoiceDetail(
            SettInOutDetailRequest(
                Prefs.getInt(
                    SharePrefsKey.ACCOUNT_CODE, 0
                ), arguments?.getString(Const.VOUCHER_NUMBER, "")
            )
        )
        call.enqueue(object : Callback<SellInOutDetailResponse> {
            override fun onResponse(
                call: Call<SellInOutDetailResponse>,
                response: Response<SellInOutDetailResponse>
            ) {
                (requireActivity() as BaseActivity).requestDidFinish()
                if (response.isSuccessful) {
                    val it = response.body()
                    if (it?.Ststus == 1) {
                        it.dataList?.let { it1 -> setSellInOutAdapter(it1) }
                    } else {
                        requireActivity().showToast(it?.ErrorMessage)
                    }
                } else {
                    requireActivity().showToast("Something went wrong, Please try again after sometime")
                }
            }

            override fun onFailure(call: Call<SellInOutDetailResponse>, t: Throwable) {
                (requireActivity() as BaseActivity).requestDidFinish()
                requireActivity().showToast("Something went wrong, Please try again after sometime")
            }
        })
    }

    private fun setOnClickListener() {
        binding.btnPrint.setOnClickListener {
            printerListBottomSheet()
        }
    }

    private fun attachObserver() {
        handleResponse(sellInOutDetailViewModel.sellInOutDetailViewModelLiveData) {
            if (it.Ststus == 1) {
                it.dataList?.let { it1 -> setSellInOutAdapter(it1) }
            } else {
                requireActivity().showToast(it.ErrorMessage)
            }
        }
    }

    private fun setSellInOutAdapter(arrayList: ArrayList<SellInOutDetailModel>) {
        if (arrayList.size > 0) {
            totalAmountCalculation(arrayList)
            binding.txtOrderId.text = "Order #${arrayList.get(0).VoucherNo}"
            val date = requireActivity().getFormattedDate(
                arrayList.get(0).VoucherDate,
                "yyyy-MM-dd'T'HH:mm:ss",
                "MMM dd, yyyy"
            )
            binding.txtOrderDate.text = "${date}"
        }
        binding.rvListOfStock.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapterDetail =
            AdapterSelInOutDetailSummary(
                true,
                requireActivity(),
                arrayList,
                itemDelete = { item, position ->
                })
        binding.rvListOfStock.adapter = adapterDetail
    }

   /* private fun totalAmountCalculation(arrayList: ArrayList<SellInOutDetailModel>) {
        itemAmount = 0.0
        itemDiscount = 0.0
        itemTotalAmount = 0.0
        arrayList.forEach {
            itemAmount = itemAmount?.plus(it.Price!!)
            itemDiscount = itemDiscount?.plus(it.Discount!!)
        }
        itemTotalAmount = itemAmount?.minus(itemDiscount!!)

        binding.txtItemAmountValue.text =
            "${Const.CURRENCY_UNIT} " + String.format("%.2f", itemAmount).toDouble().toString()
        binding.txtDiscountValue.text =
            "${Const.CURRENCY_UNIT} " + String.format("%.2f", itemDiscount).toDouble().toString()
        binding.txtTotalAmountValue.text =
            "${Const.CURRENCY_UNIT} " + String.format("%.2f", itemTotalAmount).toDouble().toString()
    }*/
    private fun totalAmountCalculation(arrayList: ArrayList<SellInOutDetailModel>) {
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

}