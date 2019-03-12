package kr.kwonho87.cardview.cardview.item

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.item_view.view.*
import kr.kwonho87.cardview.R
import kr.kwonho87.cardview.cardview.util.Utils

/**
 * kwonho87@gmail.com
 * 2019-03-12
 */
class ItemView constructor(context: Context) : RelativeLayout(context) {

    var position: Int = -1
    private lateinit var value: String

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.item_view, this)

        bgColor.setBackgroundColor(Utils.getRandomColor())
    }

    fun initFlipAnimation(position: Int) {
        this.position = position
    }

    fun onClick() {
        Toast.makeText(context, position, Toast.LENGTH_SHORT).show()
    }

    fun getValue(): String {
        return this.value
    }

    fun setData(position: Int, value: String) {
        this.position = position
        this.value = value

        title.text = value
    }
}
