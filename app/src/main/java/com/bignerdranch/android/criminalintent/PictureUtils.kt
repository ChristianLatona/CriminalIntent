package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import androidx.exifinterface.media.ExifInterface
import kotlin.math.roundToInt

fun getScaledBitmap(path: String, activity: Activity): Bitmap? {
    val size = Point()

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R){
        activity.display?.getRealSize(size)
    }else{
        @Suppress("DEPRECATION")
        activity.windowManager.defaultDisplay.getSize(size)
    }

    return getScaledBitmap(path, size.x, size.y)
}

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap? {
    // read in the dimensions of the image on disk
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true // ?? read the doc, but it's self explanatory
    BitmapFactory.decodeFile(path,options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    // figure out how much to scale down by
    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        val sampleScale = if(heightScale > widthScale){
            heightScale
        }else{
            widthScale
        }
        inSampleSize = sampleScale.roundToInt()
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    // read it and create the final Bitmap
    val bitmap = BitmapFactory.decodeFile(path,options)
    return applyCorrectRotation(path, bitmap)
}

fun applyCorrectRotation(path: String, bitmap: Bitmap): Bitmap? {
    val ei = ExifInterface(path)
    val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED)

    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
        ExifInterface.ORIENTATION_NORMAL -> bitmap
        else -> bitmap
    }
}

private fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
            matrix, true)
}
