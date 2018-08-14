package hakkon.sshdrive

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.Uri
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log
import java.io.File
import java.lang.Exception
import java.net.NetworkInterface
import java.net.URI

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
                .setNegativeButton("No", cb)
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
        val volume= sm.getStorageVolume(File(path))
        return if (volume.uuid != null) volume.uuid else ""
    }
}