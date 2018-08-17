package hakkon.sshdrive

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.dialogfragment_edit_path.view.*

typealias OnEditFinished = (path: StoredPath) -> Unit

class EditPathDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(path: StoredPath, isNew: Boolean, listener: OnEditFinished): EditPathDialogFragment {
            val fragment = EditPathDialogFragment()
            fragment.listener = listener

            val bundle = Bundle()
            bundle.putParcelable("path", path)
            bundle.putBoolean("isNew", isNew)
            fragment.arguments = bundle

            return fragment
        }
    }

    private lateinit var listener: OnEditFinished
    private lateinit var path: StoredPath
    private var isNew = false
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = activity as Context
        path = arguments!!.getParcelable("path")
        isNew = arguments!!.getBoolean("isNew")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = LayoutInflater.from(activity).inflate(R.layout.dialogfragment_edit_path, null)
        initLayout(layout)

        val builder = AlertDialog.Builder(activity)
                .setTitle(if (isNew) "New Path" else "Editing ${path.name}")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply") {_, _ ->  finishedEdit(layout)}
                .setView(layout)

        return builder.create()
    }

    private fun initLayout(layout: View) {
        layout.inputLabel.editText!!.setText(path.name)
        layout.inputUsername.editText!!.setText(path.username)
        layout.txtPath.text = path.path

        // Dropdown
        val adapter = ArrayAdapter.createFromResource(
                ctx, R.array.spinner_authentication, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        layout.spinnerAuth.adapter = adapter

        // Set buttons onclick
        // TODO: Browse
        layout.btnBrowse.setOnClickListener {  }

        if (Util.getInternalStoragePath(ctx) != null) {
            layout.btnInternalStorage.visibility = View.VISIBLE
            layout.btnInternalStorage.setOnClickListener { layout.txtPath.text = Util.getInternalStoragePath(ctx) }
        }
        if (Util.getSDCardPath(ctx) != null) {
            layout.btnSDCard.visibility = View.VISIBLE
            layout.btnSDCard.setOnClickListener { layout.txtPath.text = Util.getSDCardPath(ctx) }
        }
    }

    private fun finishedEdit(layout: View) {
        val newPath = StoredPath()
        newPath.username = layout.inputUsername.editText!!.text.toString()
        newPath.name = layout.inputLabel.editText!!.text.toString()
        newPath.path = layout.txtPath.text.toString()
        listener(newPath)
    }
}