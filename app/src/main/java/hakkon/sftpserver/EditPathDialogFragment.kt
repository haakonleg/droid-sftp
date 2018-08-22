package hakkon.sftpserver

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import hakkon.sftpserver.directorypicker.DirectoryPickerActivity
import kotlinx.android.synthetic.main.dialogfragment_edit_path.view.*

typealias OnEditFinished = (path: StoredPath) -> Unit
typealias OnPathDeleted = (path: StoredPath) -> Unit

class EditPathDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(path: StoredPath, isNew: Boolean, editListener: OnEditFinished, deleteListener: OnPathDeleted?): EditPathDialogFragment {
            val fragment = EditPathDialogFragment()
            fragment.editListener = editListener
            fragment.deleteListener = deleteListener

            val bundle = Bundle()
            bundle.putParcelable("path", path)
            bundle.putBoolean("isNew", isNew)
            fragment.arguments = bundle

            return fragment
        }
    }

    private lateinit var editListener: OnEditFinished
    private var deleteListener: OnPathDeleted? = null
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
                .setView(layout)

        return builder.create()
    }

    private fun initLayout() {
        layout.inputLabel.editText!!.setText(path.name)
        layout.inputUsername.editText!!.setText(path.username)
        layout.txtPath.text = path.path
        layout.inputPassword.editText!!.setText(path.password)

        // Set buttons onclick
        if (!isNew) {
            layout.btnDelete.visibility = View.VISIBLE
            layout.btnDelete.setOnClickListener {
                Util.showYesNoDialog(ctx, "Are you sure?", "Are you sure you want to delete this path?", DialogInterface.OnClickListener { _, _ ->
                    deleteListener?.invoke(path)
                    dismiss()
                })
            }
        }

        layout.btnCancel.setOnClickListener { dismiss() }
        layout.btnApply.setOnClickListener { finishedEdit() }

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

    private fun validate(username: String, name: String, password: String, path: String): Boolean {
        var error = false
        if (username.length < 3 ) {
            layout.inputUsername.error = getString(R.string.error_3_chars)
            error = true
        } else layout.inputUsername.isErrorEnabled = false

        if (name.length < 3) {
            layout.inputLabel.error = getString(R.string.error_3_chars)
            error = true
        } else layout.inputLabel.isErrorEnabled = false

        if (password.length < 3) {
            layout.inputPassword.error = getString(R.string.error_3_chars)
            error = true
        } else layout.inputPassword.isErrorEnabled = false

        if (path.isEmpty()) {
            layout.txtPath.error = getString(R.string.error_invalid_path)
            layout.txtPath.requestFocus()
            error = true
        } else layout.txtPath.error = null

        if (error) return false
        return true
    }

    private fun finishedEdit() {
        val username = layout.inputUsername.editText!!.text.toString()
        val name = layout.inputLabel.editText!!.text.toString()
        val path = layout.txtPath.text.toString()
        val password = layout.inputPassword.editText!!.text.toString()

        if (!validate(username, name, password, path)) return

        editListener(StoredPath(username, name, path, password))
        dismiss()
    }

    // For directory picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == DirectoryPickerActivity.RESULT_CODE_SELECTED) {
            layout.txtPath.text = data?.getStringExtra(DirectoryPickerActivity.RESULT_DIR)
        }
    }
}