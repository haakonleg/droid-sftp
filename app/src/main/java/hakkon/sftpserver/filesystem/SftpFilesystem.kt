package hakkon.sftpserver.filesystem

import android.net.Uri
import android.util.Log
import org.apache.sshd.common.file.util.BaseFileSystem
import java.nio.file.FileStore
import java.nio.file.Path
import java.nio.file.attribute.UserPrincipalLookupService

class SftpFilesystem(private val fileSystemProvider: SftpFilesystemProvider, private val contentResolverUri: Uri?, private val root: Path) : BaseFileSystem<SftpPath>(fileSystemProvider) {

    private val localFilesystem = root.fileSystem

    fun getRoot(): Path {
        return root
    }

    fun getContentResolverUri(): Uri? {
        return contentResolverUri
    }

    override fun create(root: String?, names: MutableList<String>): SftpPath {
        return SftpPath(this, root, names)
    }

    override fun supportedFileAttributeViews(): MutableSet<String> {
        return localFilesystem.supportedFileAttributeViews()
    }

    override fun isOpen(): Boolean {
        return localFilesystem.isOpen
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
        return localFilesystem.userPrincipalLookupService
    }

    override fun getFileStores(): MutableIterable<FileStore> {
        return localFilesystem.fileStores
    }

    override fun close() {
        fileSystemProvider.removeFilesystem(root)
        Log.e(this::class.simpleName, "Close")
    }
}