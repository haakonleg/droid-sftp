package hakkon.sshdrive

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Load default preferences
        PreferenceManager.setDefaultValues(this, getString(R.string.app_name),
                Context.MODE_PRIVATE, R.xml.preferences, false)

        // Instantiate ViewPager and TabLayout
        fragmentPager.adapter = PagerAdapter(supportFragmentManager)
        fragmentPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(fragmentTabLayout))
        fragmentTabLayout.addOnTabSelectedListener(this)
    }

    override fun onPause() {
        super.onPause()

        // Save paths on app close
        PathsManager.get(this).save(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabSelected(tab: TabLayout.Tab) {
        fragmentPager.currentItem = tab.position
    }

    private class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private enum class FRAGMENTS(val value: Int) {
            STATUS_FRAGMENT(0), PATHS_FRAGMENT(1), SETTINGS_FRAGMENT(2)
        }

        override fun getItem(position: Int): Fragment? {
            when (position) {
                FRAGMENTS.STATUS_FRAGMENT.value -> return StatusFragment()
                FRAGMENTS.PATHS_FRAGMENT.value -> return PathsFragment()
                FRAGMENTS.SETTINGS_FRAGMENT.value -> return SettingsFragment()
                else -> return StatusFragment()
            }
        }

        override fun getCount(): Int {
            return FRAGMENTS.values().size
        }
    }
}