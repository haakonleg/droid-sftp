package hakkon.sshdrive

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import java.io.File
import java.net.NetworkInterface

object Util {
    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    fun getLocalIPAddress(): String {
        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
            for (ipAddr in networkInterface.inetAddresses) {
                if (!ipAddr.isLoopbackAddress && !ipAddr.isLinkLocalAddress) {
                    return ipAddr.hostAddress
                }
            }
        }
        return ""
    }

    fun showAlertDialog(context: Context, message: String) {
        val dialog = AlertDialog.Builder(context)
                .setTitle("Alert")
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .create()
        dialog.show()
    }

    fun showYesNoDialog(context: Context, title: String, message: String, cb: DialogInterface.OnClickListener) {
        val dialog = AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", cb)
                .setNegativeButton("No", null)
                .create()
        dialog.show()
    }

    fun getSDCardVolume(context: Context): StorageVolume? {
        val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        for (volume in sm.storageVolumes) {
            if (volume.isRemovable) {
                return volume
            }
        }
        return null
    }

    fun getPathUUID(context: Context, path: String): String {
        val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val volume = sm.getStorageVolume(File(path))
        return if (volume != null && volume.uuid != null) volume.uuid else ""
    }

    fun getInternalStoragePath(context: Context): String? {
        val dirs = context.getExternalFilesDirs(null)
        if (dirs.isNotEmpty() && Environment.getExternalStorageState(dirs[0]) == Environment.MEDIA_MOUNTED) {
            val i = dirs[0].absolutePath.indexOf("/Android/data")
            return dirs[0].absolutePath.substring(0, i)
        }
        return null
    }

    fun getSDCardPath(context: Context): String? {
        val dirs = context.getExternalFilesDirs(null)
        if (dirs.size > 1 && Environment.getExternalStorageState(dirs[1]) == Environment.MEDIA_MOUNTED) {
            val i = dirs[1].absolutePath.indexOf("/Android/data")
            return dirs[1].absolutePath.substring(0, i)
        }
        return null
    }
}