package drwdrd.ktdev.starfield

import android.content.Context
import androidx.fragment.app.Fragment

open class MenuFragment : Fragment() {

    private var onMenuFragmentInteractionListener : OnMenuFragmentInteractionListener? = null

    interface OnMenuFragmentInteractionListener {
        fun onMenuFragmentInteraction(menu : String, item : String)
    }

    protected fun onMenuFragmentInteraction(menu : String, item : String) {
        onMenuFragmentInteractionListener?.onMenuFragmentInteraction(menu, item)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnMenuFragmentInteractionListener) {
            onMenuFragmentInteractionListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        onMenuFragmentInteractionListener = null
    }

}