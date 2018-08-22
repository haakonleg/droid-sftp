package hakkon.sshdrive

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_path.view.*

class PathsRecyclerAdapter(private val listener: OnPathEditListener, private val paths: MutableList<StoredPath>, private val context: Context) : RecyclerView.Adapter<PathsRecyclerAdapter.ViewHolder>() {
    interface OnPathEditListener {
        fun onPathEdit(path: StoredPath)
        fun onPathEnabled(path: StoredPath, enabled: Boolean)
    }

    fun addItem(path: StoredPath) {
        paths.add(path)
        notifyItemInserted(paths.size-1)
    }

    fun removeItem(path: StoredPath) {
        val i = paths.indexOf(path)
        if (i != -1) {
            paths.removeAt(i)
            notifyItemRemoved(i)
        }
    }

    fun itemUpdated(orig: StoredPath, newPath: StoredPath) {
        val i = paths.indexOf(orig)
        if (i != -1 ){
            paths[i] = newPath
            notifyItemChanged(i)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_path, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(paths[position])
    }

    override fun getItemCount(): Int {
        return paths.size
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(path: StoredPath) {
            itemView.txtName.text = path.name
            itemView.switchEnable.isChecked = path.enabled
            itemView.txtUsername.text = path.username
            itemView.txtAuth.text = context.getString(R.string.password)
            itemView.txtPath.text = path.path

            // Enabled switch listener
            itemView.switchEnable.setOnCheckedChangeListener { _, isChecked ->
                listener.onPathEnabled(paths[adapterPosition], isChecked)
            }
            // OnClick lister for edit path
            itemView.btnEdit.setOnClickListener {
                listener.onPathEdit(paths[adapterPosition])
            }
        }
    }
}