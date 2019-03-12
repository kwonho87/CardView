package kr.kwonho87.cardview.cardview.util

import android.content.Context
import android.graphics.Color
import java.util.*

class Utils {

    companion object {

        fun convertDipToPixels(context: Context, dips: Float): Int {
            return (dips * context.resources.displayMetrics.density + 0.5f).toInt()
        }

        fun getRandomColor(): Int {
            val rnd = Random()
            return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        }
    }
}