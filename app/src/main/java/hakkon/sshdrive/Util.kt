package hakkon.sshdrive

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
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

    fun firstLaunch(context: Context): Boolean {
        val prefs = getPrefs(context)
        if (prefs.getBoolean("firstLaunch", true)) {
            prefs.edit().putBoolean("firstLaunch", false).apply()
            return true
        }
        return false
    }

    fun showAlertDialog(context: Context, message: String, cb: DialogInterface.OnClickListener) {
        val dialog = AlertDialog.Builder(context)
                .setTitle("Alert")
                .setMessage(message)
                .setNegativeButton("Exit", cb)
                .setPositiveButton("Ok", cb)
                .create()

        dialog.show()
    }
}