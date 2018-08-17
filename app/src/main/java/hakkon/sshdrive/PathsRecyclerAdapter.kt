package hakkon.sshdrive

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_path.view.*

class PathsRecyclerAdapter(private val listener: OnPathEditListener) : RecyclerView.Adapter<PathsRecyclerAdapter.ViewHolder>() {
    interface OnPathEditListener {
        fun onPathEdit(path: StoredPath)
    }

    private var paths = emptyList<StoredPath>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_path, parent, false)
        return ViewHolder(view)
    }

    fun setContent(paths: List<StoredPath>) {
        this.paths = paths
        notifyDataSetChanged()
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

            // TODO: Change
            itemView.txtAuth.text = "Password"

            itemView.txtPath.text = path.path

            // OnClick lister for edit path
            itemView.btnEdit.setOnClickListener { listener.onPathEdit(paths[adapterPosition]) }
        }
    }
}