package hakkon.sshdrive

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_paths.view.*

class PathsFragment : Fragment() {
    private lateinit var ctx: Context
    private lateinit var adapter: PathsRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = activity as Context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_paths, container, false)
        adapter = PathsRecyclerAdapter()
        view.pathsRecycler.adapter = adapter
        return view
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser)
            adapter.setContent(PathsManager.get(ctx).getPaths())
    }
}