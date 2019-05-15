package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import drwdrd.ktdev.engine.Log

class SettingsFragment : Fragment() {

    interface FragmentHandler {
        fun onViewCreated(fragmentManager : FragmentManager)
        fun onResetSettings()
    }

    class SinglePaneFragmentHandler : FragmentHandler {

        private lateinit var particlesSettingsFragment: ParticlesSettingsFragment
        private lateinit var cameraSettingsFragment: CameraSettingsFragment
        private lateinit var systemSettingsFragment: SystemSettingsFragment

        override fun onViewCreated(fragmentManager : FragmentManager) {
            particlesSettingsFragment = ParticlesSettingsFragment()
            cameraSettingsFragment = CameraSettingsFragment()
            systemSettingsFragment = SystemSettingsFragment()
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.settingsContentFrame, particlesSettingsFragment)
            transaction.add(R.id.settingsContentFrame, cameraSettingsFragment)
            transaction.add(R.id.settingsContentFrame, systemSettingsFragment)
            transaction.commit()
        }

        override fun onResetSettings() {
            particlesSettingsFragment.resetSettings()
            cameraSettingsFragment.resetSettings()
            systemSettingsFragment.resetSettings()
        }
    }

    class MultiPaneFragmentHandler : FragmentHandler {

        override fun onViewCreated(fragmentManager: FragmentManager) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onResetSettings() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private lateinit var fragmentHandler : FragmentHandler


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        Log.debug("SettingsFragment", "layout tag: ${view.tag}")
        fragmentHandler = SinglePaneFragmentHandler()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentHandler.onViewCreated(childFragmentManager)
    }

    fun resetSettings() {
        SettingsProvider.resetSettings()
        fragmentHandler.onResetSettings()
    }

}
