package hakkon.sftpserver

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import hakkon.sftpserver.filesystem.SftpFilesystemProvider
import org.apache.sshd.common.file.FileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory
import java.io.File
import java.nio.file.FileSystems

class SFTPService : Service() {
    enum class ACTIONS {
        START_FOREGROUND, STOP_FOREGROUND
    }

    companion object {
        private const val CHANNEL_ID = "SFTPSERVER"
        private const val CHANNEL_NAME = "Sftp Server"

        // Binds to SFTPService
        fun bindService(context: Context, connection: ServiceConnection): Boolean {
            val intent = Intent(context, SFTPService::class.java)
            return context.bindService(intent, connection, 0)
        }

        // Starts the SFTPService in the foreground
        fun startService(context: Context) {
            val intent = Intent(context, SFTPService::class.java)
            intent.putExtra("action", ACTIONS.START_FOREGROUND)
            context.startForegroundService(intent)
        }

        // Stops the SFTPService
        fun stopService(context: Context) {
            val intent = Intent(context, SFTPService::class.java)
            intent.putExtra("action", ACTIONS.STOP_FOREGROUND)
            context.startService(intent)
        }
    }

    private lateinit var server: SshServer
    private lateinit var notification: Notification
    private lateinit var mBinder: SFTPBinder
    private lateinit var fsProvider: SftpFilesystemProvider
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        prefs = Util.getPrefs(this)
        server = initSFTPServer()
        notification = createNotification()
        mBinder = SFTPBinder()
        fsProvider = SftpFilesystemProvider(this)
    }

    override fun onDestroy() {
        server.stop()
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.getSerializableExtra("action")
            when (action) {
                ACTIONS.START_FOREGROUND -> startSFTPService()
                ACTIONS.STOP_FOREGROUND -> stopSFTPService()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        // Notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            val nManager = getSystemService(NotificationManager::class.java)
            nManager.createNotificationChannel(channel)
        }

        // Content intent
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(CHANNEL_NAME)
                .setContentText("Server is running")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build()
    }

    private fun startSFTPService() {
        Log.e(this.javaClass.name, "Starting SFTP Service")
        startForeground(1, notification)
        server.start()
    }

    private fun stopSFTPService() {
        Log.e(this.javaClass.name, "Stopping SFTP Service")
        stopForeground(true)
        stopSelf()
    }


    private fun initSFTPServer(): SshServer {
        val appdir = filesDir
        System.setProperty("user.home", appdir.absolutePath)

        val sftpServer = SshServer.setUpDefaultServer()
        sftpServer.port = prefs.getString("server_port", getString(R.string.prefs_port_default))!!.toInt()
        sftpServer.keyPairProvider = SimpleGeneratorHostKeyProvider(File(appdir, "hostkey"))

        // Password authentication
        sftpServer.passwordAuthenticator = PasswordAuthenticator { username, password, _ ->
            val path = PathsManager.get(this).getPathByUsername(username)
            if (path != null)
                password == path.password
            else
                false
        }

        // Set filesystem for each user
        sftpServer.fileSystemFactory = FileSystemFactory { session ->
            val path = PathsManager.get(this).getPathByUsername(session.username)
            if (path != null && path.enabled)
                fsProvider.newFileSystem(path.path)
            else
                FileSystems.getDefault()
        }

        // Sftp
        val sftpFactory = SftpSubsystemFactory.Builder()
        sftpFactory.withShutdownOnExit(true)
        sftpServer.subsystemFactories = listOf(sftpFactory.build())

        return sftpServer
    }

    fun getPort(): Int {
        return server.port
    }

    inner class SFTPBinder : Binder() {
        fun getService() : SFTPService {
            return this@SFTPService
        }
    }
}