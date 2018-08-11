package hakkon.sshdrive

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Instantiate ViewPager and TabLayout
        fragmentPager.adapter = PagerAdapter(supportFragmentManager)
        fragmentPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(fragmentTabLayout))
        fragmentTabLayout.addOnTabSelectedListener(this)
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    override fun onPause() {
        super.onPause()

        // Save paths on app close
        PathsManager.get(this).save(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabSelected(tab: TabLayout.Tab) {
        fragmentPager.setCurrentItem(tab.position)
    }

    // Checks permissions and requests them
    private fun checkPermissions() {
        // Get storage permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    private class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private enum class FRAGMENTS(val value: Int) {
            STATUS_FRAGMENT(0), PATHS_FRAGMENT(1), SETTINGS_FRAGMENT(2)
        }

        override fun getItem(position: Int): Fragment? {
            when (position) {
                FRAGMENTS.STATUS_FRAGMENT.value -> return StatusFragment()
                FRAGMENTS.PATHS_FRAGMENT.value -> return PathsFragment()
                //FRAGMENTS.SETTINGS_FRAGMENT.value -> return SettingsFragment()
                else -> return StatusFragment()
            }
        }

        override fun getCount(): Int {
            return FRAGMENTS.values().size
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Util.showAlertDialog(this, "We need external storage permission to read/write internal storage and SD card", DialogInterface.OnClickListener {_, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> checkPermissions()
                    DialogInterface.BUTTON_NEGATIVE -> finish()
                }
            })
        }
    }
}