package drwdrd.ktdev.starfield

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsListFragment : Fragment() {

    interface OnSettingSelectedListener {
        fun onSettingSelected(pos : Int)
    }

    var onSettingSelectedListener : OnSettingSelectedListener? = null

    private lateinit var listView: ListView

    private var selectedItemPosition : Int = 0
        set(value) {
            onSettingSelectedListener?.onSettingSelected(value)
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val namesRes = resources.getStringArray(R.array.list_settings_category)

        val categories = Array<String>(namesRes.size) { namesRes[it] }

        listView = view.findViewById(R.id.settingsListView)
        listView.adapter = SettingsAdapter(context!!, categories)
        listView.setOnItemClickListener {
            parent, view, position, id ->
            selectedItemPosition = position
            (listView.adapter as SettingsAdapter).notifyDataSetChanged()
        }
        listView.setSelection(0)
        listView.setItemChecked(0, true)
    }

    inner class SettingsAdapter(context: Context, items : Array<String>) : ArrayAdapter<String>(context, android.R.layout.simple_expandable_list_item_1, items) {

        private val inflater = LayoutInflater.from(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: inflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false)

            val text = view as TextView
            val item = getItem(position)
            text.text = item

            if(selectedItemPosition == position) {
                view.background = context.resources.getDrawable(R.color.toggleButtonColor)
            } else {
                view.background = context.resources.getDrawable(android.R.color.transparent)
            }

            return view
        }

    }
}