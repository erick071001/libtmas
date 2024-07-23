package com.tmas.tmas

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import com.tmas.tmas.R

import java.util.*


object AppUtils {
    private const val LIMIT_SPEED_STATIC = 3f
    private var KEY_IMAGE_WIDTH = "image_width"
    private var KEY_IMAGE_HEIGHT = "image_height"
    val RANGE_VALID_SPEED = LIMIT_SPEED_STATIC.toInt()..150
    val RANGE_VALID_SPEED_MOBILE_EYE = LIMIT_SPEED_STATIC.toInt()..300
    private var mToast: Toast? = null
    private var mediaPlayer: MediaPlayer? = null
    private val soundQueue: Queue<Int> = LinkedList()
    private var isPlaying: Boolean = false
    private var isMute : Boolean = false
    fun enqueueSound(context: Context,soundResId: Int) {
        if (!soundQueue.contains(soundResId)) {
            if (!isMute) {
                soundQueue.offer(soundResId)
                playNextSoundIfNotPlaying(context)
            }
        }
    }
    private fun playNextSoundIfNotPlaying(context: Context) {
        if (!isPlaying && soundQueue.isNotEmpty()) {
            val nextSound = soundQueue.peek();
            context?.let { nextSound?.let { it1 -> playAlertSound(it, it1) } }
        }
    }
    private fun playAlertSound(context: Context, soundResId: Int) {
        isPlaying = true // Đánh dấu rằng âm thanh đang được phát
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false // Đánh dấu rằng âm thanh đã phát xong
            soundQueue.poll();
            playNextSoundIfNotPlaying(context) // Phát âm thanh tiếp theo trong hàng đợi
        }
        mediaPlayer?.start()
    }

    fun getHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun getWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }



    fun checkNight(): Boolean { //day = false, night = true
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        Log.d("binhnk", "checkNight: $hour")
        if (hour in 6..17) return false
        return true
    }

    fun openApp(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
            }
        } else {
            toast(context, "Ứng dụng chưa được cài đặt", false)
        }
    }



    fun msToKmH(ms: Float): Float {
        val kmH = ms * 3.6f
        return if (kmH <= LIMIT_SPEED_STATIC) 0f else kmH
    }

    fun isPackageInstalled(packageName: String, context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun toast(context: Context?, message: String, isLengthLong: Boolean) {
        mToast?.cancel()
        mToast = Toast.makeText(
            context,
            message,
            if (isLengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )
        mToast?.show()
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun getThumbnailOfSong(context: Context?, uri: String?, dpSize: Int): Bitmap? {
        var art: Bitmap? = null
        val px = dpToPx(dpSize)
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, Uri.parse(uri))
            val rawArt: ByteArray? = mmr.embeddedPicture
            if (null != rawArt) {
                art = decodeSampledBitmapFromResource(rawArt, px, px)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return art
    }

    fun decodeSampledBitmapFromResource(rawArt: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, options)
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, options)
    }
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight
                && halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


}
