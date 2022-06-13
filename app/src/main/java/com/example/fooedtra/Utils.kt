package com.example.fooedtra

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.util.Base64.encodeToString
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.util.Base64;

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createCustomTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

fun createFile(application: Application): File {
    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir

    return File(outputDirectory, "$timeStamp.jpg")
}

fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
    val matrix = Matrix()
    return if (isBackCamera) {
        matrix.postRotate(90f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        matrix.postRotate(-90f)
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val myFile = createCustomTempFile(context)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()

    return myFile
}

fun reduceFileImage(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)

    var compressQuality = 100
    var streamLength: Int

    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > 1000000)

    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))

    return file
}

fun fileUriToBase64(uri: Uri, resolver: ContentResolver): String? {
    var encodedBase64: String? = ""
    try {
        val bytes = readBytes(uri, resolver)
        encodedBase64 = Base64.encodeToString(bytes, 0)
    } catch (e1: IOException) {
        e1.printStackTrace()
    }
    return encodedBase64
}

@Throws(IOException::class)
private fun readBytes(uri: Uri, resolver: ContentResolver): ByteArray {
    // this dynamically extends to take the bytes you read
    val inputStream = resolver.openInputStream(uri)
    val byteBuffer = ByteArrayOutputStream()

    // this is storage overwritten on each iteration with bytes
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)

    // we need to know how may bytes were read to write them to the
    // byteBuffer
    var len = 0
    while (inputStream!!.read(buffer).also { len = it } != -1) {
        byteBuffer.write(buffer, 0, len)
    }

    // and then we can return your byte array.
    return byteBuffer.toByteArray()
}