/*
 * Copyright 2019, KwonHo Lee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    private var data: ArrayList<String>? = null
    private var viewHolder = SparseArray<View>()


    fun setData(data: ArrayList<String>) {
        this.data = data
    }

    fun getData(): ArrayList<String> {
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