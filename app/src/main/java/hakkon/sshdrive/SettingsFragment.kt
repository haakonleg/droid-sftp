package hakkon.sshdrive

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.app_name)
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val serverPort = findPreference("server_port")
        serverPort.setOnPreferenceChangeListener { preference, newValue ->
            val port = (newValue as String).toInt()
            if (port >= 1024) true
            else {
                Util.showAlertDialog(activity as Context, getString(R.string.error_port))
                false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(ColorDrawable(Color.TRANSPARENT))
        setDividerHeight(0)
    }
}