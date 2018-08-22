package hakkon.sftpserver.directorypicker

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hakkon.sftpserver.R
import java.io.File
import java.util.*
import kotlinx.android.synthetic.main.directory_picker_directory.view.*
import java.nio.file.Paths

typealias onDirectoryChanged = (dir: String) -> Unit

class DirectoriesRecyclerAdapter(initialDir: String, private val onDirectoryChanged: onDirectoryChanged) : RecyclerView.Adapter<DirectoriesRecyclerAdapter.ViewHolder>() {
    private val dirStack = Stack<String>()
    private var directoryListing: List<String>

    init {
        directoryListing = getDirectoryListing(dirStack.push(initialDir))
    }

    fun upOneDir() {
        if (dirStack.size > 1) {
            dirStack.pop()
            changeDir(dirStack.peek())
        }
    }

    fun getCurrentDir(): String {
        return dirStack.peek()
    }

    private fun getDirectoryListing(dir: String): List<String> {
        val d = File(dir)
        val dirs = mutableListOf<String>()
        val files = d.listFiles()
        if (files != null) {
            for (f in files) {
                if (f.isDirectory)
                    dirs.add(f.absolutePath)
            }
        }
        return dirs
    }

    private fun changeDir(dir: String) {
        directoryListing = getDirectoryListing(dir)
        onDirectoryChanged(dir)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.directory_picker_directory, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return directoryListing.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(directoryListing[position])
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(dirPath: String) {
            view.txtDirPath.text = Paths.get(dirPath).fileName.toString()
            view.setOnClickListener { changeDir(dirStack.push(dirPath)) }
        }
    }
}