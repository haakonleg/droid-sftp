package hakkon.sshdrive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OnBootReciever : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED)
            return

        SFTPService.startService(context)
    }
}