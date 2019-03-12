package kr.kwonho87.cardview.cardview.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import kr.kwonho87.cardview.cardview.item.ItemView

/**
 * kwonho87@gmai.com
 * 2019-03-12
 */
class CardViewAdapter constructor(context: Context) : BaseAdapter() {

    private var context = context
    private lateinit var data: ArrayList<String>

    override fun getCount(): Int {
        return data.size
    }

    fun setData(data: ArrayList<String>) {
        this.data = data
    }

    fun getData(): ArrayList<String> {
        return this.data
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView

        if (view == null) {
            view = ItemView(context)
            view.apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
        }

        (view as ItemView).setData(position, data[position])

        return view
    }

    override fun getItem(arg0: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }
}