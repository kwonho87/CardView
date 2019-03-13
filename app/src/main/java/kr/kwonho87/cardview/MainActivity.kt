package kr.kwonho87.cardview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var values = LinkedHashMap<Int, String>()
        values[0] = "0"
        values[1] = "1"
        values[2] = "2"
        values[3] = "3"
        values[4] = "4"
        values[5] = "5"
        values[6] = "6"
        values[7] = "7"
        values[8] = "8"

        cardView.setData(values)
    }
}
