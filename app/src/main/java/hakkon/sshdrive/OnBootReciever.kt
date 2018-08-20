package hakkon.sshdrive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OnBootReciever : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED)
            return
        if (!Util.getPrefs(context).getBoolean("server_start_on_boot", false))
            return

        SFTPService.startService(context)
    }
}