package hakkon.sshdrive.directorypicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import hakkon.sshdrive.R
import kotlinx.android.synthetic.main.activity_directory_picker.*

class DirectoryPickerActivity : AppCompatActivity() {
    companion object {
        const val INITIAL_DIR = "/"
        const val REQUEST_DIR = 100
        const val RESULT_DIR = "RESULT_DIR"
        const val RESULT_CODE_SELECTED = 101
    }

    private lateinit var adapter: DirectoriesRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directory_picker)

        txtCurrentDir.text = INITIAL_DIR

        adapter = DirectoriesRecyclerAdapter(INITIAL_DIR) { dir -> txtCurrentDir.text = dir }
        directoriesRecycler.adapter = adapter

        // Buttons
        btnUpwards.setOnClickListener { adapter.upOneDir() }
        btnCancel.setOnClickListener { finish() }
        btnSelect.setOnClickListener { dirSelected(adapter.getCurrentDir()) }
    }

    private fun dirSelected(dir: String) {
        intent.putExtra(RESULT_DIR, dir)
        setResult(RESULT_CODE_SELECTED, intent)
        finish()
    }
}