package com.example.projectcapstone

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun hideSystemUI(activity: AppCompatActivity) {
    activity.supportActionBar?.hide()
}

fun showSystemUI(activity: AppCompatActivity){
    activity.supportActionBar?.show()
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

fun createTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    return File.createTempFile(timeStamp, ".jpg", storageDir)
}


fun rotateFileForCamera(file: File, isBackCamera: Boolean, rotation : Int) {
    val matrix = Matrix()
    val bitmap = BitmapFactory.decodeFile(file.path)

    val rotateValue = when{
        isBackCamera && rotation == Surface.ROTATION_0 -> 90f
        rotation == Surface.ROTATION_90 -> 0f
        !isBackCamera && rotation == Surface.ROTATION_0 -> -90f
        rotation == Surface.ROTATION_270 -> -180f
        else -> 180f
    }

    matrix.postRotate(rotateValue)

    if (!isBackCamera ) {
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
    }

    val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
}

fun rotateFileForGallery(file : File){
    val matrix = Matrix()
    val bitmap = BitmapFactory.decodeFile(file.path)

    val exif = ExifInterface(file.path)
    val rotation = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
        ExifInterface.ORIENTATION_UNDEFINED -> 0f
        ExifInterface.ORIENTATION_NORMAL -> 0f
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    matrix.postRotate(rotation)
    val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
}


fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver

    val myFile = createTempFile(context)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream

    val outputStream: OutputStream = FileOutputStream(myFile)

    val buf = ByteArray(1024)

    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) {
        outputStream.write(buf, 0, len)
    }

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

fun reduceFileImageFast(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)

    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, FileOutputStream(file))

    return file
}

fun fileToByteArray(myFile : File) : ByteArray {
    val stream = ByteArrayOutputStream()
    val fis = FileInputStream(myFile)
    val buffer = ByteArray(1024)
    var length: Int
    while (fis.read(buffer).also { length = it } != -1) {
        stream.write(buffer, 0, length)
    }
    return stream.toByteArray()
}

fun byteArrayToFile (context: Context, byteArray: ByteArray) : File {
    val fileOutputStream = context.openFileOutput("myFile", Context.MODE_PRIVATE)
    fileOutputStream.write(byteArray)
    fileOutputStream.close()
    return context.getFileStreamPath("myFile")
}
