package hakkon.sshdrive

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_status.*

class StatusFragment : Fragment(), ServiceConnection {
    private var sftpService: SFTPService? = null
    private var isBound = false
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = activity as Context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnStartStop.setOnClickListener { onStartClicked() }
    }

    override fun onResume() {
        super.onResume()

        // Bind to service
        SFTPService.bindService(ctx, this)
    }

    override fun onPause() {
        super.onPause()

        // Unbind service
        ctx.unbindService(this)
    }

    // Start service
    fun onStartClicked() {
        if (!isBound) {
            SFTPService.bindService(ctx, this)
            SFTPService.startService(ctx)
        } else {
            SFTPService.stopService(ctx)
        }
    }

    private fun setRunningStatus() {
        // Set button and status
        btnStartStop.text = "Stop"
        txtServerStatus.text = "Server is running"

        // Set status text
        val ip = Util.getLocalIPAddress()
        val sb = StringBuilder()
        sb
                .append("Server listening on $ip port 2222\n")
                .append("\n")
                .append("PATHS\n")
                .append("------------\n")

        for (path in PathsManager.get(ctx).getPaths()) {
            if (path.enabled)
                sb.append("${path.path} accessible on ${path.username}@${ip}\n")
        }

        txtStatus.text = sb.toString()
    }

    private fun setStoppedStatus() {
        // Set button and status
        btnStartStop.text = "Start"
        txtServerStatus.text = "Server is stopped"
        txtStatus.text = "Not running"
    }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        Log.e(this::javaClass.name, "Connected to SFTPService")
        val binder = service as SFTPService.SFTPBinder
        sftpService = binder.getService()

        setRunningStatus()
        isBound = true
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        Log.e(this::javaClass.name, "Disconnected to SFTPService")

        setStoppedStatus()
        isBound = false
    }
}