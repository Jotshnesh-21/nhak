package com.sellinout.utils

import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


object CameraUtils {
	const val IMAGE_FOLDER = "LymoDriver"
	fun getPickImageChooserIntent(activityContext: Context): Intent? {
		val allIntents: ArrayList<Intent> = ArrayList()
		val packageManager: PackageManager = activityContext.packageManager
		val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		val listCam: List<ResolveInfo> = packageManager.queryIntentActivities(captureIntent, 0)
		for (res in listCam) {
			val intent = Intent(captureIntent)
			intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
			intent.setPackage(res.activityInfo.packageName)
			allIntents.add(intent)
		}
		val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
		galleryIntent.type = "image/*"
		val listGallery: List<ResolveInfo> = packageManager.queryIntentActivities(galleryIntent, 0)
		for (res in listGallery) {
			val intent = Intent(galleryIntent)
			intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
			intent.setPackage(res.activityInfo.packageName)
			allIntents.add(intent)
		}
		var mainIntent: Intent? = allIntents[allIntents.size - 1]
		for (intent in allIntents) {
			if (intent.component!!.className == "com.android.documentsui.DocumentsActivity") {
				mainIntent = intent
				break
			}
		}
		allIntents.remove(mainIntent)
		val chooserIntent = Intent.createChooser(mainIntent, "")
		chooserIntent.putExtra(
			Intent.EXTRA_INITIAL_INTENTS,
			allIntents.toArray(arrayOfNulls<Parcelable>(allIntents.size))
		)
		return chooserIntent
	}

	@RequiresApi(Build.VERSION_CODES.KITKAT)
	fun getImageFile(data: Intent?, activityContext: Context): File? {
		return if (null != data?.data) {
			getPath(data.data!!, activityContext)?.let { imageUri ->
				File(imageUri)
			}
		} else {
			val selectedImage = data?.extras?.get("data") as? Bitmap?
			saveImageToStorage(selectedImage, activityContext)
			File(getLatestImagePath(activityContext) ?: "")
		}
	}

	private fun saveImageToStorage(bitmap: Bitmap?, activityContext: Context) {
		var imageOutStream: OutputStream? = null
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			val values = ContentValues()
			values.put(
				MediaStore.Images.Media.DISPLAY_NAME,
				"profile.jpg"
			)
			values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
			values.put(
				MediaStore.Images.Media.RELATIVE_PATH,
				Environment.DIRECTORY_PICTURES.toString() + "/" + IMAGE_FOLDER
			)
			val uri: Uri? = activityContext.contentResolver.insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				values
			)
			uri?.let {
				imageOutStream = activityContext.contentResolver.openOutputStream(it)
			}

		} else {
			var imagesDir: String? = null
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
				imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/" + IMAGE_FOLDER

			}else{
				imagesDir = Environment.getExternalStorageDirectory().toString() + "/" + IMAGE_FOLDER

			}
			val image = File(imagesDir, "profile.jpg")
			if (image.parentFile?.exists() == false)
				image.parentFile?.mkdirs()
			if (!image.exists())
				image.createNewFile()
			imageOutStream = FileOutputStream(image)
		}
		bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream!!)
		imageOutStream?.close()
	}

	private fun getLatestImagePath(activityContext: Context): String? {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			val imageColumns = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
			val imageOrderBy = MediaStore.Images.Media._ID + " DESC"
			val imageCursor: Cursor? = activityContext.contentResolver.query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				imageColumns,
				null,
				null,
				imageOrderBy
			)
			imageCursor?.moveToFirst()
			val imageFilePath = imageCursor?.getColumnIndex(MediaStore.Images.Media.DATA)
				?.let { imageCursor.getString(it) }
			imageCursor?.close()
			return imageFilePath
		} else {
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
				.toString() + "/" + IMAGE_FOLDER + "/profile.jpg"
		}

	}
}