package hakkon.sshdrive

import android.content.Context
import android.os.Environment
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class StoredPath(
        var username: String = "",
        var name: String = "",
        var path: String = "",
        var enabled: Boolean = true) : Parcelable

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
    private val paths = mutableListOf<StoredPath>()
    private val pathsType = object : TypeToken<List<StoredPath>>() {}.type

    // Reads stored paths from preferences
    private fun readStoredPaths(context: Context) {
        val prefs = Util.getPrefs(context)
        val json = prefs.getString("paths", null)

        if (json != null) {
            val storedPaths: List<StoredPath> = Gson().fromJson(json, pathsType)
            paths.addAll(storedPaths)
        }
    }

    // Saves the paths list to preferences
    fun save(context: Context) {
        val prefs = Util.getPrefs(context)
        val json = Gson().toJson(paths, pathsType)
        prefs.edit().putString("paths", json).apply()
    }

    fun getPaths(): List<StoredPath> {
        return paths
    }

    fun getPath(u: String): StoredPath? {
        for (path in paths) {
            if (path.username == u)
                return path
        }
        return null
    }
}