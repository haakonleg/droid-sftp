package hakkon.sshdrive

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class Path(
        val username: String = "",
        val name: String = "",
        val path: String = "",
        val enabled: Boolean = true)

class PathsManager private constructor() {
    companion object {
        val USER_INTERNAL = "INTERNAL"
        val USER_SDCARD = "SDCARD"

        private val instance = PathsManager()

        fun get(context: Context): PathsManager {
            if (!instance.isInitialized) {
                instance.readStoredPaths(context.applicationContext)
                instance.isInitialized = true
            }
            return instance
        }
    }

    // Check whether initialized or not
    private var isInitialized = false

    // List of user/home dir
    private val paths = mutableListOf<Path>()
    private val pathsType = object : TypeToken<List<Path>>() {}.type

    // Reads stored paths from preferences
    private fun readStoredPaths(context: Context) {
        val prefs = Util.getPrefs(context)
        val json = prefs.getString("paths", null)

        if (json != null) {
            val storedPaths: List<Path> = Gson().fromJson(json, pathsType)
            paths.addAll(storedPaths)
        }
    }

    // Finds internal storage and SD card directories and adds them
    fun addExternalPaths(context: Context) {
        val dirs = context.getExternalFilesDirs(null)

        // Get the root dirs instead of app dir
        for (i in dirs.indices) {
            val index = dirs[i].absolutePath.indexOf("/Android/data")
            dirs[i] = File(dirs[i].absolutePath.substring(0, index))

            // Internal storage
            if (i == 0 && dirs.isNotEmpty() && Environment.getExternalStorageState(dirs[0]) == Environment.MEDIA_MOUNTED)
                paths.add(Path(USER_INTERNAL, "Internal Storage", dirs[0].absolutePath))

            // SD Card
            if (i == 1 && dirs.size > 1 && Environment.getExternalStorageState(dirs[1]) == Environment.MEDIA_MOUNTED)
                paths.add(Path(USER_SDCARD, "SD Card", dirs[1].absolutePath))
        }
    }

    // Saves the paths list to preferences
    fun save(context: Context) {
        val prefs = Util.getPrefs(context)
        val json = Gson().toJson(paths, pathsType)
        prefs.edit().putString("paths", json).apply()
    }

    fun getPaths(): List<Path> {
        return paths
    }

    fun getPath(u: String): Path? {
        for (path in paths) {
            if (path.username == u)
                return path
        }
        return null
    }
}