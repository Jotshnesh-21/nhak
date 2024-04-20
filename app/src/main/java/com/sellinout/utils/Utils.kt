package com.sellinout.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.KITKAT)
fun getPath(uriImage: Uri, context: Context): String? {
    var uri = uriImage
    val needToCheckUri = Build.VERSION.SDK_INT >= 19
    var selection: String? = null
    var selectionArgs: Array<String>? = null
    if (needToCheckUri && DocumentsContract.isDocumentUri(context, uri)) {
        if (isExternalStorageDocument(uri)) {
            val docId: String = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/" + split[1]
            } else {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
        } else if (isDownloadsDocument(uri)) {
            val id: String = DocumentsContract.getDocumentId(uri)
            if (id.startsWith("raw:")) {
                return id.replaceFirst("raw:".toRegex(), "")
            }
            uri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)

            )
        } else if (isMediaDocument(uri)) {
            val docId: String = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            when (type) {
                "image" -> uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            selection = "_id=?"
            selectionArgs = arrayOf(
                split[1]
            )
        }
    }
    if ("content".equals(uri.scheme, ignoreCase = true)) {
        val projection = arrayOf(
            MediaStore.Images.Media.DATA
        )
        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val columnIndex =
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        return cursor.getString(columnIndex)
                    }
                }
        } catch (e: java.lang.Exception) {
            Log.e("on getPath", "Exception", e)
        }
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        return uri.path
    }
    return null
}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

fun getRealPathFromURI(context: Context, contentURI: Uri): String {
    val result: String
    val projection = arrayOf(MediaStore.Video.Media.DATA)
    val cursor = context.contentResolver.query(
        contentURI, projection,
        null, null, null
    )
    if (cursor == null) { // Source is Dropbox or other similar local file path
        result = contentURI.path ?: ""
    } else {
        cursor.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
        result = cursor.getString(idx)
        cursor.close()
    }
    return result
}
