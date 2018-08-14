package hakkon.sshdrive.filesystem

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.support.v4.provider.DocumentFile
import android.util.Log
import hakkon.sshdrive.Util
import java.io.*
import java.net.URI
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.FileChannel
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider
import java.util.concurrent.ExecutorService

class SftpFilesystemProvider(context: Context) : FileSystemProvider() {

    private val ctx = context.applicationContext
    private val contentResolver = ctx.contentResolver
    private val filesystems = hashMapOf<Path, SftpFilesystem>()

    private val contentResolverUriCache = hashMapOf<Path, Uri>()

    fun newFileSystem(path: String): FileSystem {
        return newFileSystem(URI(path), emptyMap<String, Any>())
    }

    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        val path = ensureDirectory(Paths.get(uri.toString()).toAbsolutePath())

        // Check if this is SD card path
        var contentResolverUri: Uri? = null
        val prefs = Util.getPrefs(ctx)
        val pathUUID = Util.getPathUUID(ctx, uri.toString())
        val sdCardUUID = prefs.getString("grantedUUID", null)

        if (pathUUID == sdCardUUID) {
            contentResolverUri = Uri.parse(prefs.getString("sdCardURI", null))
            Log.e("Got SD Card URI", contentResolverUri.toString())
        }

        return synchronized(filesystems) {
            if (filesystems.containsKey(path)) {
                throw FileSystemAlreadyExistsException("$path already mapped")
            }
            val fs = SftpFilesystem(this, contentResolverUri, path, env)
            filesystems.put(path, fs)

            Log.e(this::class.simpleName, "Created filesystem $path")
            fs
        }
    }

    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        val r = realPath(path)
        val p = r.fileSystem.provider()
        return p.checkAccess(r, *modes)
    }

    override fun newInputStream(path: Path, vararg options: OpenOption): InputStream {
        Log.e(this::class.simpleName, "newInputStream ${path}")

        // If using contentresolver
        val cr = (path as SftpPath).getContentResolverUri()
        if (cr != null) {
            val file = resolveContentResolverUri(cr, path)
            return contentResolver.openInputStream(file)
        }

        val r = realPath(path)
        val p = r.fileSystem.provider()
        return p.newInputStream(r, *options)
    }

    override fun copy(path: Path, path2: Path, vararg options: CopyOption) {
        Log.e(this::class.simpleName, "copy $path $path2")

        // If using contentresolver
        val cr = (path as SftpPath).getContentResolverUri()
        if (cr != null) {
            val source = resolveContentResolverUri(cr, path)
            val sourceParent = resolveContentResolverUri(cr, path.parent)
            DocumentsContract.copyDocument(contentResolver, source, sourceParent)
            return
        }

        val r = realPath(path)
        val r2 = realPath(path2)
        val p = r.fileSystem.provider()
        p.copy(r, r2, *options)
    }

    override fun <V : FileAttributeView?> getFileAttributeView(path: Path, type: Class<V>, vararg options: LinkOption): V {
        val r = realPath(path)
        val p = r.fileSystem.provider()
        return p.getFileAttributeView(r, type, *options)
    }

    override fun isSameFile(path: Path, path2: Path): Boolean {
        Log.e(this::class.simpleName, "isSameFile $path $path2")
        val r = realPath(path)
        val r2 = realPath(path2)
        val p = r.fileSystem.provider()
        return p.isSameFile(r, r2)
    }

    override fun newAsynchronousFileChannel(path: Path, options: MutableSet<out OpenOption>?, executor: ExecutorService, vararg attrs: FileAttribute<*>): AsynchronousFileChannel {
        throw UnsupportedOperationException("newAsynchronousFileChannel")
    }

    override fun getScheme(): String {
        return "droidsftp"
    }

    override fun isHidden(path: Path): Boolean {
        Log.e(this::class.simpleName, "isHidden ${path}")
        val r = realPath(path)
        val p = r.fileSystem.provider()
        return p.isHidden(r)
    }

    override fun newDirectoryStream(dir: Path, filter: DirectoryStream.Filter<in Path>): DirectoryStream<Path> {
        Log.e(this::class.simpleName, "newDirectoryStream ${dir}")
        val r = realPath(dir)
        val p = r.fileSystem.provider()
        return root((dir as SftpPath).fileSystem, p.newDirectoryStream(r, filter))
    }

    protected fun root(sfs: SftpFilesystem, ds: DirectoryStream<Path>): DirectoryStream<Path> {
        return object : DirectoryStream<Path> {
            override fun iterator(): MutableIterator<Path> {
                return root(sfs, ds.iterator())
            }

            override fun close() {
                ds.close()
            }
        }
    }

    protected fun root(sfs: SftpFilesystem, iter: Iterator<Path>): MutableIterator<Path> {
        return object : MutableIterator<Path> {
            override fun hasNext(): Boolean {
                return iter.hasNext()
            }

            override fun next(): Path {
                return root(sfs, iter.next())
            }

            override fun remove() {
                throw UnsupportedOperationException()
            }
        }
    }

    protected fun root(sfs: SftpFilesystem, nat: Path): Path {
        if (nat.isAbsolute) {
            val root = sfs.getRoot()
            val rel = root.relativize(nat)
            return sfs.getPath("/$rel")
        }
        return sfs.getPath(nat.toString())
    }

    override fun newByteChannel(path: Path, options: MutableSet<out OpenOption>, vararg attrs: FileAttribute<*>): SeekableByteChannel {
        throw UnsupportedOperationException("newByteChannel")
    }

    override fun delete(path: Path) {
        Log.e(this::class.simpleName, "delete ${path}")

        // If using contentresolver
        val cr = (path as SftpPath).getContentResolverUri()
        if (cr != null) {
            val file = resolveContentResolverUri(cr, path)
            DocumentsContract.deleteDocument(contentResolver, file)
            return
        }

        val r = realPath(path)
        val p = r.fileSystem.provider()
        p.delete(r)
    }

    override fun <A : BasicFileAttributes?> readAttributes(path: Path, type: Class<A>, vararg options: LinkOption): A {
        val r = realPath(path)
        val p = r.fileSystem.provider()
        return p.readAttributes(r, type, *options)
    }

    override fun readAttributes(path: Path, attributes: String, vararg options: LinkOption?): MutableMap<String, Any> {
        val r = realPath(path)
        val p = r.fileSystem.provider()
        return p.readAttributes(r, attributes, *options)
    }

    override fun deleteIfExists(path: Path): Boolean {
        throw UnsupportedOperationException("deleteIfExists")
    }

    override fun createLink(link: Path?, existing: Path?) {
        throw UnsupportedOperationException("createLink")
    }

    override fun newOutputStream(path: Path, vararg options: OpenOption): OutputStream {
        Log.e(this::class.simpleName, "newInputStream ${path}")

        // If using contentresolver
        val cr = (path as SftpPath).getContentResolverUri()
        if (cr != null) {
            val file = resolveContentResolverUri(cr, path)
            return contentResolver.openOutputStream(file)
        }

        val r = realPath(path)
        val p = r.fileSystem.provider()
        return p.newOutputStream(r, *options)
    }

    override fun getFileSystem(uri: URI): FileSystem {
        return getFileSystem(Paths.get(uri.toString()))
    }

    protected fun getFileSystem(path: Path): SftpFilesystem {
        val r = realPath(path)

        return synchronized(filesystems) {
            var fsInstance: SftpFilesystem? = null
            var rootInstance: Path? = null
            for(fse in filesystems) {
                val root = fse.key
                val fs = fse.value

                if (r == root) {
                    fsInstance = fs
                    break
                }
                if (!r.startsWith(root)) {
                    continue
                }
                if (rootInstance == null || rootInstance.nameCount < root.nameCount) {
                    rootInstance = root
                    fsInstance = fs
                }
            }

            if (fsInstance == null) {
                throw FileSystemNotFoundException("Filesystem not found: $r")
            }
            fsInstance
        }
    }

    override fun readSymbolicLink(link: Path?): Path {
        throw UnsupportedOperationException("readSymbolicLink")
    }

    override fun getPath(uri: URI): Path {
        val str = uri.schemeSpecificPart
        val i = str.indexOf("!/")
        if (i == -1) {
            throw IllegalArgumentException("URI: $uri does not contain path info")
        }

        val fs = getFileSystem(uri)
        val subPath = str.substring(i + 1)
        return fs.getPath(subPath)
    }

    override fun createSymbolicLink(link: Path?, target: Path?, vararg attrs: FileAttribute<*>?) {
        throw UnsupportedOperationException("createSymbolicLink")
    }

    override fun newFileChannel(path: Path, options: MutableSet<out OpenOption>, vararg attrs: FileAttribute<*>): FileChannel {
        Log.e(this::class.simpleName, "newFileChannel ${path} ${options.toList()}")

        // If using contentresolver
        val cr = (path as SftpPath).getContentResolverUri()
        if (cr != null) {
            val file: Uri?
            var channel: FileChannel? = null

            // If file must be created first
            if (options.contains(StandardOpenOption.CREATE) || options.contains(StandardOpenOption.CREATE_NEW)) {
                val root = resolveContentResolverUri(cr, path.parent)
                file = DocumentsContract.createDocument(contentResolver, root, null, path.fileName.toString())
            } else {
                file = resolveContentResolverUri(cr, path)
            }

            // Open for write
            if (options.contains(StandardOpenOption.WRITE)) {
                val fd = if (options.contains(StandardOpenOption.APPEND))
                    contentResolver.openFileDescriptor(file, "wa") else contentResolver.openFileDescriptor(file, "w")

                channel = FileOutputStream(fd.fileDescriptor).channel

                if (options.contains(StandardOpenOption.TRUNCATE_EXISTING))
                    channel.truncate(0)

            // Open for read
            } else if (options.contains(StandardOpenOption.READ)) {
                val fd = contentResolver.openFileDescriptor(file, "r")
                channel = FileInputStream(fd.fileDescriptor).channel
            }

            if (channel != null) return channel
        }

        val r = realPath(path)
        val p = r.fileSystem.provider()
        return p.newFileChannel(r, options, *attrs)
    }

    override fun getFileStore(path: Path): FileStore {
        Log.e(this::class.simpleName, "getFileStore ${path}")
        val root = getFileSystem(path).getRoot()
        return Files.getFileStore(root)
    }

    override fun setAttribute(path: Path, attribute: String, value: Any, vararg options: LinkOption) {
        Log.e(this::class.simpleName, "setAttribute $path $value")
        val r = realPath(path)
        val p = r.fileSystem.provider()
        return p.setAttribute(r, attribute, value, *options)
    }

    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        Log.e(this::class.simpleName, "move $source $target")

        // If using contentresolver
        val cr = (source as SftpPath).getContentResolverUri()
        if (cr != null) {
            val sourceUri = resolveContentResolverUri(cr, source)
            val sourceParentUri = resolveContentResolverUri(cr, source.parent)
            val targetParentUri = resolveContentResolverUri(cr, target.parent)
            if (sourceParentUri != targetParentUri) {
                DocumentsContract.moveDocument(contentResolver, sourceUri, sourceParentUri, targetParentUri)
            } else {
                DocumentsContract.renameDocument(contentResolver, sourceUri, target.fileName.toString())
            }
            return
        }

        val s = realPath(source)
        val t = realPath(target)
        val p = s.fileSystem.provider()
        p.move(s, t, *options)
    }

    override fun createDirectory(path: Path, vararg attrs: FileAttribute<*>?) {
        Log.e(this::class.simpleName, "createDirectory $path")

        // If using contentresolver
        val cr = (path as SftpPath).getContentResolverUri()
        if (cr != null) {
            val root = resolveContentResolverUri(cr, path.parent)
            DocumentsContract.createDocument(contentResolver, root, DocumentsContract.Document.MIME_TYPE_DIR, path.fileName.toString())
            return
        }

        val r = realPath(path)
        val p = r.fileSystem.provider()
        p.createDirectory(r, *attrs)
    }

    private fun resolveContentResolverUri(root: Uri, path: Path): Uri {
        val fromCache = contentResolverUriCache[path]
        if (fromCache != null) {
            return fromCache
        }

        var resolved: DocumentFile = DocumentFile.fromTreeUri(ctx, root)
        for(i in 0 until path.nameCount) {
            resolved = resolved.findFile(path.getName(i).toString())
        }

        val resolvedUri = resolved.uri
        contentResolverUriCache.put(path, resolvedUri)
        return resolvedUri
    }

    private fun realPath(path: Path): Path {
        if (path !is SftpPath) {
            throw ProviderMismatchException("$path is not a ${SftpPath::class.simpleName} but a ${path::class.simpleName}")
        }

        val absolute = path.toAbsolutePath()
        val root = absolute.fileSystem.getRoot()

        val subPath = absolute.toString().substring(1)
        return root.resolve(subPath).normalize().toAbsolutePath()
    }

    protected fun ensureDirectory(path: Path): Path {
        val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
        if (!attrs.isDirectory) {
            throw UnsupportedOperationException("$path is not a directory")
        }
        return path
    }
}