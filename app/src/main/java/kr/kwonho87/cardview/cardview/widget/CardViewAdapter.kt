package kr.kwonho87.cardview.cardview.widget

import android.content.Context
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import android.util.SparseArray
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

    private var max: Int = 0
    private var context = context
    private var data: LinkedHashMap<Int, String>? = null
    private var viewHolder = SparseArray<View>()


    fun setData(data: LinkedHashMap<Int, String>) {
        this.data = data
    }

    fun getData(): LinkedHashMap<Int, String> {
        return this.data!!
    }

    fun setMaxCount(max: Int) {
        this.max = max

        for (index in 0 until max) {
            var view = ItemView(context)
            view.apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
            viewHolder.put(index, view)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = viewHolder[position % max] as ItemView
        view.setData(position, data!![position]!!)

        return view
    }

    override fun getCount(): Int {
        return data!!.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }
}