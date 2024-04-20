package com.sellinout.utils

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class FileDownloader {
    private val MEGABYTE = 1024 * 1024

    fun downloadFile(fileUrl: String?, directory: File?) : String? {
        try {
            val url = URL(fileUrl)
            val urlConnection = url.openConnection() as HttpURLConnection
            //urlConnection.setRequestMethod("GET");
            //urlConnection.setDoOutput(true);
            urlConnection.connect()
            val inputStream = urlConnection.inputStream
            val fileOutputStream = FileOutputStream(directory)
            val totalSize = urlConnection.getContentLength()
            val buffer = ByteArray(MEGABYTE)
            var bufferLength = 0
            while (inputStream.read(buffer).also { bufferLength = it } > 0) {
                fileOutputStream.write(buffer, 0, bufferLength)
            }
            fileOutputStream.close()
            return ""
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return "File Not Found"
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return e.message
        } catch (e: IOException) {
            e.printStackTrace()
            return e.message
        }
    }
}