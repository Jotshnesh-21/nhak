package com.sellinout.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.sellinout.base.BaseActivity
import com.sellinout.base.BaseFragment
import com.sellinout.code.Resource
import com.sellinout.code.Status
import com.sellinout.network.ErrorResolver.handleErrorResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.random.Random


//API SETUP
fun <R> Flow<R>.launchInBackGround(
    coroutineScope: CoroutineScope, liveData: MutableLiveData<R>? = null
): Job {
    return if (liveData != null) {
        onEach {
            liveData.postValue(it)
        }.flowOn(Dispatchers.IO).launchIn(coroutineScope)
    } else {
        flowOn(Dispatchers.IO).launchIn(coroutineScope)
    }

}


fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) {
    if (this is Fragment) {
        liveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer(body))
    } else {
        liveData.observe(this, androidx.lifecycle.Observer(body))
    }
}

fun <R> LifecycleOwner.handleResponse(
    liveData: LiveData<Resource<R>>,
    requiredProgress: Boolean = true,
    @UiThread process: (R) -> Unit

) {
    if (liveData.hasActiveObservers()) {
        return
    }

    observe(liveData) {
        it?.apply {
            when (this.status) {
                Status.SUCCESS -> {
                    data?.let { it1 -> process.invoke(it1) }

                    if (requiredProgress) this@handleResponse.hideProgress()
                }

                Status.ERROR -> {
                    throwable?.let { exception ->
                        if (this@handleResponse is AppCompatActivity) {
                            handleErrorResponse(
                                this@handleResponse, null, retrofitResponse, throwable, message
                            )
                        } else if (this@handleResponse is Fragment) {
                            handleErrorResponse(
                                this@handleResponse.requireActivity(),
                                this@handleResponse,
                                retrofitResponse,
                                throwable,
                                message
                            )
                        }
                    }
                    if (requiredProgress) this@handleResponse.hideProgress()
                }

                Status.LOADING -> {
//                        handleLoadingDialog(LoadingDialog.LoadingStates.Loading)
                    if (requiredProgress) this@handleResponse.showProgress()
                }

                else -> {
                    if (requiredProgress) this@handleResponse.hideProgress()
                }
            }
        }
    }


}

fun LifecycleOwner.hideProgress() {
    if (this is BaseActivity) {
        this.requestDidFinish()
    } else if (this is BaseFragment) {
        (requireActivity() as BaseActivity).requestDidFinish()
    } else if (this is DialogFragment) {
        (requireActivity() as BaseActivity).requestDidFinish()
    }

}

fun LifecycleOwner.showProgress() {
    if (this is BaseActivity) {
        this.requestDidStart()
    } else if (this is BaseFragment) {
        (requireActivity() as BaseActivity).requestDidStart()
    } else if (this is DialogFragment) {
        (requireActivity() as BaseActivity).requestDidFinish()
    }

}
//**************************** END API SETUP


fun Any.toJson() = Gson().toJson(this)
inline fun <reified T> String.fromJson(): T? = Gson().fromJson(this, T::class.java)

fun Context.showToast(strMessage: String? = null) {
    Toast.makeText(this, strMessage ?: "", Toast.LENGTH_SHORT).show()
}

inline fun <reified T> Context.navigateClearStack() {
    startActivity(Intent(this, T::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
}

@ExperimentalContracts
fun Any?.isListAndEmpty(): Boolean {
    contract {
        returns(true) implies (this@isListAndEmpty is List<*>)
    }
    return this is List<*> && this.isEmpty()
}

fun Context.getCurrentDate(dateFormat: String?): String? {
    val dateObj: Date = Calendar.getInstance().time
    val postFormat = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    return postFormat.format(dateObj)
}

fun Context.getFormattedDate(
    inputDate: String?, inputDateFormat: String?, outputDateFormat: String?
): String? {
    return try {
        val inputFormat = SimpleDateFormat(inputDateFormat, Locale.ENGLISH)
        val outputFormat = SimpleDateFormat(outputDateFormat, Locale.ENGLISH)
        val date = inputDate.let { inputFormat.parse(it) }
        date.let {
            outputFormat.format(it)
        }
    } catch (e: Exception) {
        ""
    }
}

fun getSellInOutLabel(code: String): String {
    return when (code) {
        "3" -> {
            Const.IN
        }

        "9" -> {
            Const.OUT
        }

        else -> {
            "0"
        }
    }
}

fun randomNumberGenerate(): Int { //5 DIGIT RANDOM NUMBER GENERATE
    val r = Random(System.currentTimeMillis())
    val ran = (1 + r.nextInt(2)) * 10000 + r.nextInt(10000)
    Log.e("Random", ">> ${ran}")
    return ran

}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun Activity.changeStatusColor(activity: Activity, color: Int) {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activity.window.statusBarColor = color
    }
}

private val BLUETOOTH_PERMISSION_REQUEST_CODE = 1
fun Context.checkBluetoothPermissions(): Boolean {
    return (ContextCompat.checkSelfPermission(
        this, Manifest.permission.BLUETOOTH
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this, Manifest.permission.BLUETOOTH_ADMIN
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)
}

fun Context.checkBluetoothConnectPermissions(): Boolean {
    return (ContextCompat.checkSelfPermission(
        this, Manifest.permission.BLUETOOTH_CONNECT
    ) == PackageManager.PERMISSION_GRANTED)
}

fun Activity.requestBluetoothConnectPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN
            ), BLUETOOTH_PERMISSION_REQUEST_CODE
        )
    }
}

fun Activity.requestBluetoothPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), BLUETOOTH_PERMISSION_REQUEST_CODE
        )
    }
}

lateinit var currentPhotoPath: String

@Throws(IOException::class)
fun Activity.createImageFile(): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    ).apply {
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = absolutePath

    }
}

fun Activity.dispatchTakePictureIntent() {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        // Ensure that there's a camera activity to handle the intent
        takePictureIntent.resolveActivity(packageManager)?.also {
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile()

            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            galleryAddPic()
            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this, "com.example.android.fileprovider", it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                Log.e("photoURI", ">> $photoURI")
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                startActivityForResult(takePictureIntent, 100)
            }
        }
    }
}

fun Activity.galleryAddPic() {
    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
        val f = File(currentPhotoPath)
        mediaScanIntent.data = Uri.fromFile(f)
        sendBroadcast(mediaScanIntent)
    }
}

fun encodeImageToBase(bitmap: Bitmap): String {
    return try {
        // initialize byte stream
        val stream = ByteArrayOutputStream()
        // compress Bitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        // Initialize byte array
        val bytes = stream.toByteArray()
        // get base64 encoded string
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: IOException) {
        e.printStackTrace()
        ""
    }
}

fun decodeBaseImage(strBaseImage: String): Bitmap {
    // decode base64 string
    val bytes: ByteArray = Base64.decode(strBaseImage, Base64.DEFAULT)
    // Initialize bitmap
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

fun Activity.openPdfUsingIntent(file: File) {
    val photoURI = FileProvider.getUriForFile(
        this, applicationContext.packageName + ".provider", file
    )
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(photoURI, "application/pdf")
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

    try {
        val chooserIntent = Intent.createChooser(intent, "Open PDF")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooserIntent)
        } else {
            Toast.makeText(
                this,
                "No PDF viewer installed. Please install a PDF viewer to open the file.",
                Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Handle exceptions, such as ActivityNotFoundException or SecurityException
    }
}

fun amountCalculation(qty: Double, price: Double, discountPer: Double): Double {
    val amountBeforeDisc = qty * price
    val discAmount = amountBeforeDisc * discountPer / 100
    return amountBeforeDisc - discAmount
}

fun discountAmountCalculation(qty: Double, price: Double, discountPer: Double): Double {
    val amountBeforeDisc = qty * price
    return amountBeforeDisc * discountPer / 100
}
