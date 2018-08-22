package hakkon.sftpserver.filesystem

import android.net.Uri
import org.apache.sshd.common.file.util.BasePath
import java.io.File
import java.nio.file.LinkOption
import java.nio.file.Path

class SftpPath(private val fileSystem: SftpFilesystem, rootPath: String?, names: List<String>) : BasePath<SftpPath, SftpFilesystem>(fileSystem, rootPath, names) {
    override fun toRealPath(vararg options: LinkOption): Path {
        val absolute = toAbsolutePath()
        val provider = fileSystem.provider()
        provider.checkAccess(absolute)
        return absolute
    }

    override fun toFile(): File {
        val absolute = toAbsolutePath()
        var path = fileSystem.getRoot()
        for (n in absolute.names) {
            path = path.resolve(n)
        }
        return path.toFile()
    }

    fun getContentResolverUri(): Uri? {
        return fileSystem.getContentResolverUri()
    }
}