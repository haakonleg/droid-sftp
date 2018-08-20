package hakkon.sshdrive

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_paths.view.*

class PathsFragment : Fragment(), PathsRecyclerAdapter.OnPathEditListener {
    private lateinit var ctx: Context
    private lateinit var adapter: PathsRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = activity as Context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_paths, container, false)
        adapter = PathsRecyclerAdapter(this, PathsManager.get(ctx).getPaths().toMutableList())
        view.pathsRecycler.adapter = adapter
        view.btnAdd.setOnClickListener { newPath() }
        return view
    }

    override fun onPathEdit(path: StoredPath) {
        val editFragment = EditPathDialogFragment.newInstance(path, false, {newPath ->
            PathsManager.get(ctx).pathUpdated(path, newPath, ctx)
            adapter.itemUpdated(path, newPath)
        }, {deletedPath ->
            PathsManager.get(ctx).removePath(path, ctx)
            adapter.removeItem(path)
        })
        editFragment.show(fragmentManager, "dialog")
    }

    override fun onPathEnabled(path: StoredPath, enabled: Boolean) {
        PathsManager.get(ctx).setEnabled(path, enabled, ctx)
    }

    private fun newPath() {
        val editFragment = EditPathDialogFragment.newInstance(StoredPath(), true, {newPath ->
            PathsManager.get(ctx).addPath(newPath, ctx)
            adapter.addItem(newPath)
        }, null)
        editFragment.show(fragmentManager, "dialog")
    }
}