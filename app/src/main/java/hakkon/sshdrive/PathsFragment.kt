package hakkon.sshdrive

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_paths.*

class PathsFragment : Fragment() {
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = activity as Context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_paths, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pathsRecycler.adapter = PathsRecyclerAdapter(PathsManager.get(ctx).getPaths())
    }
}