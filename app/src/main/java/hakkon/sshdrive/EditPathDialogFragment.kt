package hakkon.sshdrive

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import hakkon.sshdrive.directorypicker.DirectoryPickerActivity
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
    private lateinit var layout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = activity as Context
        path = arguments!!.getParcelable("path")
        isNew = arguments!!.getBoolean("isNew")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        layout = LayoutInflater.from(ctx).inflate(R.layout.dialogfragment_edit_path, null)
        initLayout()

        val builder = AlertDialog.Builder(activity)
                .setTitle(if (isNew) "New Path" else "Editing ${path.name}")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply") {_, _ ->  finishedEdit()}
                .setView(layout)

        return builder.create()
    }

    private fun initLayout() {
        layout.inputLabel.editText!!.setText(path.name)
        layout.inputUsername.editText!!.setText(path.username)
        layout.txtPath.text = path.path
        layout.inputPassword.editText!!.setText(path.password)

        // Set buttons onclick
        layout.btnBrowse.setOnClickListener {
            val intent = Intent(ctx, DirectoryPickerActivity::class.java)
            startActivityForResult(intent, DirectoryPickerActivity.REQUEST_DIR)
        }

        if (Util.getInternalStoragePath(ctx) != null) {
            layout.btnInternalStorage.visibility = View.VISIBLE
            layout.btnInternalStorage.setOnClickListener { layout.txtPath.text = Util.getInternalStoragePath(ctx) }
        }
        if (Util.getSDCardPath(ctx) != null) {
            layout.btnSDCard.visibility = View.VISIBLE
            layout.btnSDCard.setOnClickListener { layout.txtPath.text = Util.getSDCardPath(ctx) }
        }
    }

    private fun finishedEdit() {
        val username = layout.inputUsername.editText!!.text.toString()
        val name = layout.inputLabel.editText!!.text.toString()
        val path = layout.txtPath.text.toString()

        val password = layout.inputPassword.editText!!.text.toString()

        listener(StoredPath(username, name, path, password))
    }

    // For directory picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == DirectoryPickerActivity.RESULT_CODE_SELECTED) {
            layout.txtPath.text = data?.getStringExtra(DirectoryPickerActivity.RESULT_DIR)
        }
    }
}