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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import kr.kwonho87.cardview.cardview.item.ItemView
import kr.kwonho87.cardview.cardview.listener.OnSwipeTouchListener
import kr.kwonho87.cardview.cardview.util.Utils

/**
 * kwonho87@gmail.com
 * 2019-03-12
 */
class CardView constructor(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var mCardViewAdapter = CardViewAdapter(context)                 // Adapter.
    private val mMaxCount = 3                                               // The maximum number of views to show.
    private val mAniDuration = 220L                                         // Animation run time.
    private val mViewSpace = Utils.convertDipToPixels(context, 22.0f) // The spacing of views.

    /**
     * Init.
     */
    init {
        setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeUpDown(action: ACTION) {
                super.onSwipeUpDown(action)
                if (getIsEnabled()) {
                    when(action) {
                        ACTION.ACTION_UP -> goUp()
                        ACTION.ACTION_DOWN -> goDown()
                    }
                }
            }
            override fun onClick() {
                super.onClick()
                Log.d("CardView", "onClick")
            }
        })
    }

    /**
     * Set data.
     */
    fun setData(data: ArrayList<String>) {
        mCardViewAdapter.setData(data)
        mCardViewAdapter.setMaxCount(mMaxCount)

        removeAllViews()
        initAllView(data)
    }

    /**
     * Default view init.
     */
    private fun initAllView(data: ArrayList<String>) {
        var maxViewCount = if(data.size > mMaxCount) mMaxCount else data.size

        for (index in 0 until maxViewCount) {
            var position = maxViewCount - index - 1
            val scale = getScale(position, maxViewCount)
            val margin = getMargin(position)

            val view = mCardViewAdapter.getView(index, null, this)!!
            view.scaleX = scale
            view.scaleY = scale
            view.translationY = margin

            addViewInLayout(view, 0, getParams(view))
        }
    }

    /**
     * Raise the first card up.
     */
    private fun goUp() {

        // Touch Lock.
        setIsEnabled(false)

        // Move the top view up through the animation.
        val topView = getChildAt(childCount - 1)
        topView.animate()
            .translationY(-(topView.translationY + topView.height))
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(mAniDuration)
            .setInterpolator(AccelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    // If the animation of the top view ends, remove it from the screen.
                    removeView(topView)

                    // Create a new view and add it to the bottom.
                    addNewViewLast()

                    // Repeat as many child views.
                    for(index in 0 until childCount) {

                        // Get child view.
                        val view = getChildAt(index)

                        // If it is the last child view.
                        if (index == childCount - 1) {
                            bringToTop(view)

                        }
                        else {
                            val margin = getMargin(index)
                            val scale = getScale(index, childCount)
                            view.animate()
                                .translationY(margin)
                                .setInterpolator(AccelerateInterpolator())
                                .setListener(null)
                                .scaleX(scale)
                                .scaleY(scale)
                                .duration = mAniDuration
                        }
                    }

                    // Touch Unlock.
                    setIsEnabled(true)
                }
            })
    }

    /**
     * Add a new view to the 0th position.
     */
    private fun addNewViewLast() {
        var lastViewValue = (getChildAt(0) as ItemView).getValue()
        Log.d("CardView", "lastViewValue : $lastViewValue")

        var data = mCardViewAdapter.getData()

//        var nextValue = data.stream()
//            .filter { i -> i > lastViewValue }
//            .findFirst()
//            .orElse(data[0])
//
//
//
////            .forEach { i -> Log.d("CardView", "value : $i") }
//        Log.d("CardView", "nextValue : $nextValue")
//        Log.d("CardView", "next index : ${data.indexOf(nextValue)}")

        var position = 0
        for (index in 0 until data.size) {
            if(lastViewValue == data[index]) {
                position = index
            }
        }

        if(position >= data.size - 1) {
            position = 0
        }
        else {
            position++
        }
        Log.d("CardView", "position : $position")

        var view = mCardViewAdapter.getView(position, null, this)!!
        val scale = getScale(0, childCount)
        val margin = getMargin(0)

        view.scaleX = scale
        view.scaleY = scale
        view.translationY = margin
        addViewInLayout(view, 0, getParams(view))
    }

    /**
     * Move the view forward.
     */
    private fun bringToTop(view: View?) {
        view!!.animate()
            .translationY(0f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(mAniDuration)
            .interpolator = AccelerateInterpolator()
    }

    /**
     * Bring the card up again.
     */
    private fun goDown() {

        // Touch Lock.
        setIsEnabled(false)

        // Add a new view and start down animation.
        bringToDown()

        for (index in 0 until childCount) {
            val view = getChildAt(index)
            val scale = getScale(index, childCount)
            val margin = getMargin(index)

            if (index != childCount - 1) {
                view.animate()
                    .translationY(margin)
                    .setInterpolator(AccelerateInterpolator())
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(mAniDuration)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {

                            // Touch Unlock.
                            setIsEnabled(true)
                        }
                    })
            }
        }
    }

    /**
     * When you add a new view, the new down animation will work.
     */
    private fun bringToDown() {

        var view = getChildAt(0) as ItemView
        removeView(view)

        var frontView = getChildAt(childCount - 1) as ItemView
        var frontValue = frontView.getValue()

        var data = mCardViewAdapter.getData()
        var position = 0
        for (index in 0 until data.size) {
            if(frontValue == data[index]) {
                position = index
                break
            }
        }

        if(position == 0) {
            position = data.size - 1
        }
        else {
            position--
        }

        view.setData(position, mCardViewAdapter.getData()[position].toString())
        view.translationY = -2000f
        view.animate()
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .duration = mAniDuration
        addView(view)
    }

    /**
     *
     */
    private fun getScale(index: Int, childCount: Int): Float {
        val count = index % childCount
//        Log.d("CardView", "getScale : $count")

        return when(count) {
            0 -> return 0.87f
            1 -> return 0.93f
            2 -> return 1.0f
            else -> 0f
        }
    }

    /**
     *
     */
    private fun getMargin(index: Int): Float {
        return when(index) {
            0 -> return 116f
            1 -> return 58f
            2 -> return 0f
            else -> 0f
        }
    }

    /**
     * Make default Layoutparams.
     */
    private fun getParams(view: View): LayoutParams? {
        var params: FrameLayout.LayoutParams? = view.layoutParams as FrameLayout.LayoutParams?
        params.apply {
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            this!!.gravity = Gravity.CENTER
        }
        return params
    }

    /**
     * Enable / disable all views including the child view.
     */
    private fun setIsEnabled(state: Boolean) {
        for (nCount in 0 until childCount) {
            val view = getChildAt(nCount)
            view.isEnabled = state
        }
        isEnabled = state
    }

    /**
     * Get enable / disable first child view.
     */
    private fun getIsEnabled(): Boolean {
        for (index in 0 until childCount) {
            val view = getChildAt(index)
            if(!view.isEnabled) {
                return false
            }
        }
        return true
    }
}