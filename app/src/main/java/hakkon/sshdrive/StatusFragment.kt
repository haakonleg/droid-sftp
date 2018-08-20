package hakkon.sshdrive

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_status.*
import kotlinx.android.synthetic.main.view_permission.view.*

class StatusFragment : Fragment(), ServiceConnection {
    private var sftpService: SFTPService? = null
    private var isBound = false
    private lateinit var ctx: Context

    private val extPermissionId = View.generateViewId()
    private val sdPermissionId = View.generateViewId()

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

        if (layoutPermissions.childCount == 1) {
            createPermissionList()
            if (layoutPermissions.childCount == 1)
                permissionsStatus.text = "All permissions granted"
        }

        // Bind to service
        SFTPService.bindService(ctx, this)
    }

    private fun createPermissionList() {
        // Check external storage permissions
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val extPermission = layoutInflater.inflate(R.layout.view_permission, layoutPermissions, false)
            extPermission.id = extPermissionId
            extPermission.txtPermissionTitle.text = getString(R.string.permissions_external_title)
            extPermission.txtPermissionDesc.text = getString(R.string.permissions_external_desc)
            layoutPermissions.addView(extPermission)

            // Request external permission on click
            extPermission.setOnClickListener {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }

        // Check SD card permissions
        val grantedUUID = Util.getPrefs(ctx).getString("grantedUUID", null)
        val sdCardVolume = Util.getSDCardVolume(ctx)
        if (sdCardVolume != null && (sdCardVolume.uuid != grantedUUID)) {

            val sdPermission = layoutInflater.inflate(R.layout.view_permission, layoutPermissions, false)
            sdPermission.id = sdPermissionId
            sdPermission.txtPermissionTitle.text = getString(R.string.permissions_sd_title)
            sdPermission.txtPermissionDesc.text = getString(R.string.permissions_sd_desc)
            layoutPermissions.addView(sdPermission)

            // Request SD permission on click
            sdPermission.setOnClickListener {
                val intent = sdCardVolume.createAccessIntent(null)
                startActivityForResult(intent, 1)
            }
        }
    }

    // Used for external storage permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val view = layoutPermissions.findViewById<View>(extPermissionId)
            layoutPermissions.removeView(view)
            return
        }
        Util.showAlertDialog(ctx, "You did not grant permission")
    }

    // Used for SD card permission
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            Util.showAlertDialog(ctx, "You did not grant permission")
            return
        }

        val treeUri = data.data
        ctx.applicationContext.grantUriPermission(ctx.packageName, treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        ctx.applicationContext.contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        // Save URI to sharedpreferences
        val prefs = Util.getPrefs(ctx)
        val sdCardUUID = Util.getSDCardVolume(ctx)!!.uuid
        prefs.edit().putString("grantedUUID", sdCardUUID).putString("sdCardURI", treeUri.toString()).apply()

        val view = layoutPermissions.findViewById<View>(sdPermissionId)
        layoutPermissions.removeView(view)
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
        val port = sftpService!!.getPort()
        val ip = Util.getLocalIPAddress()
        val sb = StringBuilder()
        sb
                .append("Server listening on $ip port $port\n")
                .append("\n")
                .append("PATHS\n")
                .append("------------\n")

        val paths = PathsManager.get(ctx).getPaths()
        if (paths.isEmpty()) {
            sb.append("No paths configured")
        } else {
            for (path in paths) {
                if (path.enabled)
                    sb.append("${path.path} accessible on ${path.username}@${ip}\n")
            }
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