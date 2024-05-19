package com.sellinout.ui.dailysummary

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
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
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.base.BaseFragment
import com.sellinout.databinding.FragmentStockSummaryBinding
import com.sellinout.ui.MainActivity
import com.sellinout.utils.FileDownloader
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.openPdfUsingIntent
import com.sellinout.utils.showToast
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

class DailySummaryFragment : BaseFragment(R.layout.fragment_stock_summary) {
    private val calendar = Calendar.getInstance()
    private lateinit var binding: FragmentStockSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStockSummaryBinding.inflate(inflater, container, false)
        binding.txtDirectPdfDownload.isVisible = false
        binding.btnDownload.isVisible = false
        setOnClickListener()
        return binding.root
    }

    private fun setOnClickListener() {
        binding.btnDateDownload.setOnClickListener {
            showDatePicker()
        }
    }

    fun newInstance(): DailySummaryFragment {
        val args = Bundle()
        val fragment = DailySummaryFragment()
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

                downloadFile("https://nhak.logicfirst.in/Invoice/DownloadDailySummary/${
                    Prefs.getInt(
                        SharePrefsKey.ACCOUNT_CODE, 0
                    )
                }/$formattedDate", "${
                    Prefs.getInt(
                        SharePrefsKey.ACCOUNT_CODE, 0
                    )
                }_${formattedDateFileName}.pdf")
               /* if (isPermissionGranted()) {
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
*/
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {

            val isGranted =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            }else{
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            }

            if (isGranted) {
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
    }*/

    private fun downloadFile(paramsUrl: String, paramName: String) {
        val selectedDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateFormatFileName = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault())

        val formattedDate = dateFormat.format(selectedDate.time)
        val formattedDateFileName = dateFormatFileName.format(selectedDate.time)

        Log.e("paramsUrl", ">>${paramsUrl}")
        Log.e("FORMATTEDDATE", ">>${formattedDate}")
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf<String>(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    56444
                )
            }else{
                DownloadFile(
                    paramsUrl,paramName
                ).execute()
            }
        } else {
            if (!checkStorageCameraPermission13()) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf<String>(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ),
                    56444
                )
            }else{
                DownloadFile(
                    paramsUrl,paramName
                ).execute()
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

    @SuppressLint("StaticFieldLeak")
    private inner class DownloadFile(private var url: String, private var strFileName: String) :
        AsyncTask<Void, Void, File>() {

        override fun onPreExecute() {
            requireActivity().runOnUiThread {
                requireActivity().showToast(getString(R.string.downloading))
                (requireActivity() as MainActivity).requestDidStart()
            }
        }

        override fun doInBackground(vararg params: Void?): File? {
            if (!url.startsWith("http")) {
                (requireActivity() as MainActivity).requestDidFinish()
                return null
            }
            try {
                val file = File(Environment.getExternalStorageDirectory(), "Download")
                if (!file.exists()) {
                    file.mkdirs()
                }
                val result = commonDocumentDirPath(strFileName)
                sharedFilePath = result
                val downloadManager =
                    requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                val request = DownloadManager.Request(Uri.parse(url))
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                request.setDestinationUri(Uri.fromFile(result))
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                downloadManager?.enqueue(request)
            } catch (e: Exception) {
                Log.e(">>>>>", e.toString())
            }
            return sharedFilePath
        }

        override fun onPostExecute(result: File) {
            (requireActivity() as MainActivity).requestDidFinish()
            try {
                val photoURI = FileProvider.getUriForFile(
                    requireActivity(),
                    requireActivity().applicationContext.packageName + ".provider",
                    result
                )
//                val intentShareFile = Intent(Intent.ACTION_SEND)
//                intentShareFile.type = URLConnection.guessContentTypeFromName(result.name)
//                intentShareFile.putExtra(
//                    Intent.EXTRA_STREAM,
//                    photoURI
//                )
//                intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                startActivity(Intent.createChooser(intentShareFile, ""))
                if (result.exists()) requireActivity().openPdfUsingIntent(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun commonDocumentDirPath(folderName: String): File {
        var dir: File? = null
        dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/" + folderName
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/" + folderName)
        }
        return dir
    }

    var sharedFilePath: File? = null

}