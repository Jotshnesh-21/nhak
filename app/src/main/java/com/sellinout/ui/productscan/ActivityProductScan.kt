package com.sellinout.ui.productscan

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.BuildConfig
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.base.BaseFragment
import com.sellinout.data.model.request.ProductInvoiceRequest
import com.sellinout.data.model.request.SettInOutRequest
import com.sellinout.data.model.response.InvoiceNumberResponse
import com.sellinout.databinding.ScanProductActivityBinding
import com.sellinout.network.ApiService
import com.sellinout.network.RetrofitClient
import com.sellinout.ui.MainActivity
import com.sellinout.ui.cartsummary.CardSummaryActivity
import com.sellinout.utils.Const
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.amountCalculation
import com.sellinout.utils.discountAmountCalculation
import com.sellinout.utils.getCurrentDate
import com.sellinout.utils.gone
import com.sellinout.utils.handleResponse
import com.sellinout.utils.invisible
import com.sellinout.utils.randomNumberGenerate
import com.sellinout.utils.showToast
import com.sellinout.utils.toJson
import com.sellinout.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.math.roundToInt

@AndroidEntryPoint
class ActivityProductScan : BaseFragment(R.layout.scan_product_activity) {
    private val addToCartViewModel by viewModels<ProductScanViewModel>()
    private lateinit var binding: ScanProductActivityBinding
    private var barcodeDetector: BarcodeDetector? = null
    private lateinit var cameraSource: CameraSource
    private val REQUEST_CAMERA_PERMISSION = 201
    private var listOfProduct: ArrayList<ProductInvoiceRequest> = arrayListOf()
    private var vchType = "0"

    private var isScanned: Boolean = false

    override fun onPause() {
        super.onPause()
        isScanned = false
        cameraSource.release()
    }

    override fun onDestroy() {
        isScanned = false
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        listOfProduct = ArrayList()
        if (Prefs.getString(SharePrefsKey.CART_DATA, "") != "") {
            listOfProduct = Gson().fromJson(
                Prefs.getString(
                    SharePrefsKey.CART_DATA, ""
                ), object : TypeToken<ArrayList<ProductInvoiceRequest>>() {}.type
            )
            Log.e("RESUME", ">>>> ${listOfProduct.size}")
            quantityCount()
        }
        initialiseDetectorsAndSources()
    }

    fun newInstance(): ActivityProductScan {
        val args = Bundle()
        val fragment = ActivityProductScan()
        fragment.arguments = args
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ScanProductActivityBinding.inflate(inflater, container, false)
        val view = binding.root

        vchType = Const.SALE_IN_OUT

        (requireActivity() as MainActivity).bindingContainMain.txtItemCount.gone()
        setOnClick()
//        attachObserver()
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun attachObserver() {
        handleResponse(addToCartViewModel.getInvoiceViewModelLiveData) {
            Log.e("INVOICE_NUMBER", "${it.Data}")
            it.let {
                if (it.Ststus == 1) {
                    Const.voucherNumber = if (it.Data != "") {
                        it.Data.toString()
                    } else {
                        ""
                    }
                }
            }
        }

        handleResponse(addToCartViewModel.addToCartViewModelLiveData) {
            if (it.Ststus == 1) {
                requireActivity().runOnUiThread {
                    requireActivity().showToast("Item Added to the cart")
                    (requireActivity() as MainActivity).openFragment(CardSummaryActivity().newInstance())
                }
            } else {
                requireActivity().showToast(it.ErrorMessage)
            }
        }
    }

    private fun setOnClick() {
        Log.e("VouchRandom", ">>>${Const.voucherNumber}")
        if (Const.voucherNumber == "") {/* Const.voucherNumber =
                 (SharePrefsKey.getUserData().PreFix
                     ?: "") + randomNumberGenerate().toString()*//*  addToCartViewModel.getInvoiceNumber(
                  SettInOutRequest(
                      Prefs.getInt(
                          SharePrefsKey.ACCOUNT_CODE, 0
                      )
                  )
              )*/
            getInvoiceNumber()
        }
        (requireActivity() as MainActivity).bindingContainMain.imgCart.gone()
        binding.btnClickImage.setOnClickListener {
            try {
                if (ActivityCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openCamera()
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf<String>(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        binding.btnGoToCart.setOnClickListener {
            if (listOfProduct.size > 0) {
                Log.e("CARTLIST", ">> ${listOfProduct.toJson()}")
                Prefs.putString(SharePrefsKey.CART_DATA, "")
                Prefs.putString(SharePrefsKey.CART_DATA, listOfProduct.toJson())

                (requireActivity() as MainActivity).openFragment(CardSummaryActivity().newInstance())
            } else {
                requireActivity().showToast("Please Scan at least one product")
            }
        }
    }

    private fun getInvoiceNumber() {
        (requireActivity() as BaseActivity).requestDidStart()
        val apiService = RetrofitClient.getClient(
            Prefs.getString(
                SharePrefsKey.STR_BASE_URL, BuildConfig.BASE_URL
            )
        ).create(ApiService::class.java)
        val call = apiService.getInvoiceNumber(
            SettInOutRequest(
                Prefs.getInt(
                    SharePrefsKey.ACCOUNT_CODE, 0
                )
            )
        )
        call.enqueue(object : Callback<InvoiceNumberResponse> {
            override fun onResponse(
                call: Call<InvoiceNumberResponse>, response: Response<InvoiceNumberResponse>
            ) {
                (requireActivity() as BaseActivity).requestDidFinish()
                if (response.isSuccessful) {
                    val it = response.body()
                    it.let {
                        if (it?.Ststus == 1) {
                            Const.voucherNumber = if (it.Data != "") {
                                it.Data.toString()
                            } else {
                                ""
                            }
                        }
                    }
                } else {
                    requireActivity().showToast("Something went wrong, Please try again after sometime")
                }
            }

            override fun onFailure(call: Call<InvoiceNumberResponse>, t: Throwable) {
                (requireActivity() as BaseActivity).requestDidFinish()
                requireActivity().showToast("Something went wrong, Please try again after sometime")
            }
        })

    }

    private fun initialiseDetectorsAndSources() {

        barcodeDetector =
            BarcodeDetector.Builder(requireActivity()).setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()
        cameraSource = CameraSource.Builder(requireActivity(), barcodeDetector)
            .setRequestedPreviewSize(900, 900)
            .setAutoFocusEnabled(true) //you should add this feature
            .setFacing(0) // change
            .build()
        binding.surfaceView.getHolder().addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            requireActivity(), Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraSource.start(binding.surfaceView.getHolder())
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int, width: Int, height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.release()
            }
        })
        barcodeDetector?.setProcessor(object : Detector.Processor<Barcode?> {
            override fun release() {
                //Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode?>) {
                Log.e("RESUME", "isScanned: ${isScanned}")
                if (!isScanned) {
                    val barcodes: SparseArray<Barcode?> = detections.detectedItems
                    Log.e("barcodes", "barcodes: ${barcodes.size()}")
                    if (barcodes.size() != 0) {
                        val code = barcodes.valueAt(0)?.displayValue ?: ""
//                    this@ActivityProductScan.showToast(code)
                        /* EG: MDBAZIGAR,MIX,7X9,150,11.01 (SCAN CODE) OLD
                        MDBAZIGAR,MIX,7X9,150,11.01,PCS,1 (SCAN CODE) NEW
                                1. Item Name    (MDBAZIGAR)
                                2. Item Color   (MIX)
                                3. Item Size    (7X9)
                                4. Price        (150)
                                5. Discount     (11.01) %
                                6. Unit         (PCS) //REMOVED
                                7. Quantity     (1)   //REMOVED
                         */

                        val list = code.split(",")
                        if (list.isNotEmpty() && list.size == 5) {
                            Log.e("barcodes", "list: ${list.size} :: \n\n ${list.toJson()}")
                            isScanned = true/* if (Const.voucherNumber == null && Const.voucherNumber == "") {
                                 Const.voucherNumber =
                                     (SharePrefsKey.getUserData().PreFix
                                         ?: "") + randomNumberGenerate().toString()
                             }*/
                            val currentDate = requireActivity().getCurrentDate("dd-MM-yyyy")
                            val amnt =
                                amountCalculation(1.0, list[3].toDouble(), list[4].toDouble())
                            val discount = discountAmountCalculation(
                                1.0, list[3].toDouble(), list[4].toDouble()
                            )
                            val model = ProductInvoiceRequest(
                                Prefs.getInt(SharePrefsKey.ACCOUNT_CODE, 0),
                                Const.voucherNumber.toString(),
                                currentDate,
                                list[0],
                                list[1],
                                list[2],
                                "",//Const.UNIT,
                                1.0,//1.0,
                                list[4].toDouble(),
                                list[3].toDouble(),
                                discount,// list[4].toDouble(), //DISCOUNT
                                amnt,
                                vchType.toInt(),
                                Const.ITEM_TYPE_QR_CODE, //FOR QR CODE = 1 and Image = 2
                                null,
                                list[4].toDouble() // Discount Percent
                            )
                            listOfProduct.add(model)
                            requireActivity().runOnUiThread {
                                quantityCount()
//                                requireActivity().showToast("${listOfProduct.size}")
                                cameraSource.stop()
                                commonDialog {
                                    setTitleValue(getString(R.string.product_added_to_cart))
                                    setConfirmButtonText(getString(R.string.str_rescan))
                                    setCancelButtonText(getString(R.string.str_go_to_cart))
                                    cancelClick {
                                        dismissDialog()
                                        binding.btnGoToCart.performClick()
                                    }
                                    confirmClick {
                                        isScanned = false
                                        dismissDialog()
                                        if (ActivityCompat.checkSelfPermission(
                                                requireActivity(), Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            try {
                                                cameraSource.release()
                                                initialiseDetectorsAndSources()
                                                cameraSource.start(binding.surfaceView.getHolder())
                                            } catch (e: IOException) {
                                                throw RuntimeException(e)
                                            }
                                        }
                                    }
                                }.show()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun quantityCount() {
        var quantity = 0
        listOfProduct.forEach {
            quantity = (quantity + it.Quantity!!).roundToInt()
        }
        (requireActivity() as MainActivity).bindingContainMain.txtItemCount.visible()
        (requireActivity() as MainActivity).bindingContainMain.txtItemCount.text =
            "Count: ".plus(quantity)
    }

/*
    private fun amountCalculation(qty: Double, price: Double, discountPer: Double): Double {
        val amountBeforeDisc = qty * price
        val discAmount = amountBeforeDisc * discountPer / 100
        return amountBeforeDisc - discAmount
    }

    private fun discountAmountCalculation(qty: Double, price: Double, discountPer: Double): Double {
        val amountBeforeDisc = qty * price
        return amountBeforeDisc * discountPer / 100
    }
*/

    private fun openCamera() {
        Prefs.putString(SharePrefsKey.CART_DATA, listOfProduct.toJson())
        val values = ContentValues()
        Const.imageUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )!!
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Const.imageUri)
        requireActivity().startActivityForResult(intent, 1001)
    }
}