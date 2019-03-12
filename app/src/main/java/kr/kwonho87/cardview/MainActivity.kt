package kr.kwonho87.cardview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var values = ArrayList<String>()
        values.add("1")
        values.add("2")
        values.add("3")
        values.add("4")
        values.add("5")
        values.add("6")
        values.add("7")
        values.add("8")

        cardView.setData(values)
    }
}
