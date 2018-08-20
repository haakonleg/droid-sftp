package hakkon.sshdrive

import android.content.Context
import android.os.Parcelable
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoredPath(
        val username: String = "",
        val name: String = "",
        val path: String = "",
        val password: String = "",
        var enabled: Boolean = true) : Parcelable

class PathsManager private constructor() {
    companion object {
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

    // For GSON
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

    fun getPathByUsername(u: String): StoredPath? {
        return paths.firstOrNull { it.username == u }
    }

    fun setEnabled(path: StoredPath, enabled: Boolean, context: Context) {
        val i = paths.indexOf(path)
        if (i != -1) {
            paths[i].enabled = enabled
            save(context)
        }
    }

    fun pathUpdated(orig: StoredPath, newPath: StoredPath, context: Context) {
        val i = paths.indexOf(orig)
        if (i != -1) {
            paths[i] = newPath
            save(context)
        }
    }

    fun addPath(path: StoredPath, context: Context) {
        paths.add(path)
        save(context)
    }

    fun removePath(path: StoredPath, context: Context) {
        paths.remove(path)
        save(context)
    }
}