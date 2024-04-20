package com.sellinout.ui.sellinout

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.BuildConfig
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.base.BaseFragment
import com.sellinout.data.model.request.SettInOutRequest
import com.sellinout.data.model.response.LoginResponse
import com.sellinout.data.model.response.SellInOutItemModel
import com.sellinout.data.model.response.SellInOutResponse
import com.sellinout.databinding.StockListActivityBinding
import com.sellinout.network.ApiService
import com.sellinout.network.RetrofitClient
import com.sellinout.ui.MainActivity
import com.sellinout.ui.cartsummary.CardSummaryActivity
import com.sellinout.ui.productscan.ActivityProductScan
import com.sellinout.ui.sellinout.adapter.AdapterSellInOut
import com.sellinout.ui.sellinoutdetail.SellInOutDetailPage
import com.sellinout.utils.Const
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.SharePrefsKey.ACCOUNT_CODE
import com.sellinout.utils.gone
import com.sellinout.utils.handleResponse
import com.sellinout.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class ActivitySellInOutList : BaseFragment(R.layout.stock_list_activity) {

    private val sellInOutViewModel by viewModels<SellInOutViewModel>()
    private lateinit var binding: StockListActivityBinding
    private var adapterSellInOut: AdapterSellInOut? = null
    fun newInstance(): ActivitySellInOutList {
        val args = Bundle()

        val fragment = ActivitySellInOutList()
        fragment.arguments = args
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StockListActivityBinding.inflate(inflater, container, false)
        val view = binding.root
        setOnClickListener()
//        attachObserver()
//        sellInOutViewModel.getSellInOutItem(SettInOutRequest(Prefs.getInt(ACCOUNT_CODE, 0)))
        getSellInOutItem()
        return view
    }

    private fun getSellInOutItem() {
        (requireActivity() as BaseActivity).requestDidStart()
        val apiService = RetrofitClient.getClient(
            Prefs.getString(
                SharePrefsKey.STR_BASE_URL,
                BuildConfig.BASE_URL
            )
        )
            .create(ApiService::class.java)
        val call = apiService.getSellInOutItem(SettInOutRequest(Prefs.getInt(ACCOUNT_CODE, 0)))
        call.enqueue(object : Callback<SellInOutResponse> {
            override fun onResponse(
                call: Call<SellInOutResponse>,
                response: Response<SellInOutResponse>
            ) {
                (requireActivity() as BaseActivity).requestDidFinish()
                if (response.isSuccessful) {
                    val it = response.body()
                    if (it?.Ststus == 1) {
                        it.data?.saleInSaleOut?.let { it1 ->
                            setSellInOutAdapter(it1)
                        }
                        binding.txtOpeningStock.text =
                            "Opening Stock: ${it.data?.openingBalance ?: 0} pcs"
                    } else {
                        requireActivity().showToast(it?.ErrorMessage)
                    }
                } else {
                    requireActivity().showToast("Something went wrong, Please try again after sometime")

                }
            }

            override fun onFailure(call: Call<SellInOutResponse>, t: Throwable) {
                (requireActivity() as BaseActivity).requestDidFinish()
                requireActivity().showToast("Something went wrong, Please try again after sometime")
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).bindingContainMain.txtTitle.text =
            requireActivity().getString(R.string.app_name)
    }

    private fun attachObserver() {
        handleResponse(sellInOutViewModel.sellInOutViewModelLiveData) {
            if (it.Ststus == 1) {
                it.data?.saleInSaleOut?.let { it1 ->
                    setSellInOutAdapter(it1)
                }
                binding.txtOpeningStock.text = "Opening Stock: ${it.data?.openingBalance ?: 0} pcs"
            }
        }
    }

    private fun setSellInOutAdapter(list: ArrayList<SellInOutItemModel>) {
        binding.rvListOfStock.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapterSellInOut =
            AdapterSellInOut(requireActivity(), list, itemClick = { sellInOutItemModel, i ->
                (requireActivity() as MainActivity).openFragment(
                    SellInOutDetailPage().newInstance(
                        sellInOutItemModel.VoucherNo ?: "", sellInOutItemModel.vchType ?: ""
                    )
                )
            })
        binding.rvListOfStock.adapter = adapterSellInOut
    }

    private fun setOnClickListener() {
        (requireActivity() as MainActivity).bindingContainMain.imgCart.gone()
        (requireActivity() as MainActivity).bindingContainMain.txtItemCount.gone()
        binding.btnSaleOut.setOnClickListener {
            scanQrCodeScreen("9")
        }
        binding.btnSaleIn.setOnClickListener {
            scanQrCodeScreen("3")

        }
    }

    private fun scanQrCodeScreen(strMode: String) {
        requireActivity().runOnUiThread {
            //Stuff that updates the UI
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Prefs.putString(SharePrefsKey.CART_DATA, "")
                    Const.SALE_IN_OUT = strMode
                    (requireActivity() as MainActivity).openFragment(
                        ActivityProductScan().newInstance()
                    )

                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf<String>(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        56444
                    )
                }
            } else {
                if (checkStorageCameraPermission13()) {
                    Prefs.putString(SharePrefsKey.CART_DATA, "")
                    Const.SALE_IN_OUT = strMode
                    (requireActivity() as MainActivity).openFragment(
                        ActivityProductScan().newInstance()
                    )

                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf<String>(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_MEDIA_IMAGES
                        ),
                        56444
                    )
                }
            }
        }
    }

    private fun checkStorageCameraPermission13(): Boolean {
        val permissionCamera = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        )
        val permissionStorage = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.READ_MEDIA_IMAGES
        )
        return (permissionCamera == PackageManager.PERMISSION_GRANTED && permissionStorage == PackageManager.PERMISSION_GRANTED)
    }
}