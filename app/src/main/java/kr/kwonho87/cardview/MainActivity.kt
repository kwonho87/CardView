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

package kr.kwonho87.cardview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var values = ArrayList<String>()
//        values[0] = "0"
//        values[1] = "1"
//        values[2] = "2"
//        values[3] = "3"
//        values[4] = "4"
//        values[5] = "5"
//        values[6] = "6"
//        values[7] = "7"
//        values[8] = "8"
//        values[9] = "9"

//        values.add("0")
//        values.add("1")
//        values.add("2")
//        values.add("3")
//        values.add("4")
//        values.add("5")
//        values.add("6")
//        values.add("7")
//        values.add("8")

        values.add("A")
        values.add("B")
        values.add("C")
        values.add("D")
        values.add("E")
        values.add("F")
        values.add("G")
        values.add("H")
        values.add("I")

        cardView.setData(values)
    }
}
