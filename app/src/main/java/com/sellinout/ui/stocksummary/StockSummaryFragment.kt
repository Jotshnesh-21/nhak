package com.sellinout.ui.stocksummary

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.BuildConfig
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.base.BaseFragment
import com.sellinout.databinding.FragmentStockSummaryBinding
import com.sellinout.network.ApiService
import com.sellinout.network.RetrofitClient
import com.sellinout.utils.Const
import com.sellinout.utils.FileDownloader
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.openPdfUsingIntent
import com.sellinout.utils.showToast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

class StockSummaryFragment : BaseFragment(R.layout.fragment_stock_summary) {
    private val calendar = Calendar.getInstance()
    private lateinit var binding: FragmentStockSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStockSummaryBinding.inflate(inflater, container, false)
        setOnClickListener()
        return binding.root
    }

    private fun setOnClickListener() {
        binding.btnDateDownload.setOnClickListener {
            showDatePicker()
        }
        binding.btnDownload.setOnClickListener {

            if (isPermissionGranted()) {
                (requireActivity() as BaseActivity).requestDidStart()
                doMyTask(
                    "https://nhak.logicfirst.in/Item/StockSummary/${
                        Prefs.getInt(
                            SharePrefsKey.ACCOUNT_CODE, 0
                        )
                    }", "${
                        Prefs.getInt(
                            SharePrefsKey.ACCOUNT_CODE, 0
                        )
                    }.pdf"
                )
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    200
                )
            }

        }
    }

    fun newInstance(): StockSummaryFragment {
        val args = Bundle()
        val fragment = StockSummaryFragment()
        fragment.arguments = args
        return fragment
    }


    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireActivity(), { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val dateFormatFileName = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault())

                val formattedDate = dateFormat.format(selectedDate.time)
                val formattedDateFileName = dateFormatFileName.format(selectedDate.time)

//                "Selected Date: $formattedDate"

                if (isPermissionGranted()) {
                    (requireActivity() as BaseActivity).requestDidStart()
                    doMyTask(
                        "https://nhak.logicfirst.in/Item/StockSummary/${
                            Prefs.getInt(
                                SharePrefsKey.ACCOUNT_CODE, 0
                            )
                        }/$formattedDate", "${
                            Prefs.getInt(
                                SharePrefsKey.ACCOUNT_CODE, 0
                            )
                        }_${formattedDateFileName}.pdf"
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        200
                    )
                }

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calendar.getTimeInMillis()
        datePickerDialog.show()
    }

    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    private fun doMyTask(paramsUrl: String, paramName: String) {
        Log.e("PARAMURI", ">> ${paramsUrl}")
        myExecutor.execute {
            try {
                val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
                val folder = File(extStorageDirectory, getString(R.string.app_name))
                folder.mkdir()
                val pdfFile = File(folder, paramName)

                pdfFile.createNewFile()
                val strMessage = FileDownloader().downloadFile(paramsUrl, pdfFile)
                myHandler.post {
                    (requireActivity() as BaseActivity).requestDidFinish()
                    if (strMessage == "") {
                        Toast.makeText(
                            requireActivity(),
                            "File Downloaded Path: ${pdfFile.absolutePath}",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (pdfFile.exists()) requireActivity().openPdfUsingIntent(pdfFile)
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "Err: ${strMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                (requireActivity() as BaseActivity).requestDidFinish()
            }

        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                (requireActivity() as BaseActivity).requestDidStart()
                doMyTask(
                    "https://nhak.logicfirst.in/Item/StockSummary/${
                        Prefs.getInt(
                            SharePrefsKey.ACCOUNT_CODE, 0
                        )
                    }", "maven.pdf"
                )
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Permission denied! Please grant storage permission to download the file.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}