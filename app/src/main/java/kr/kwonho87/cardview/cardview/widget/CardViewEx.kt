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
import kr.kwonho87.cardview.cardview.listener.OnSwipeTouchListener
import kr.kwonho87.cardview.cardview.util.Utils

/**
 * kwonho87@gmail.com
 * 2019-03-12
 */
class CardViewEx constructor(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var mCardViewAdapter = CardViewAdapter(context)                 // Adapter.
    private val mMaxCount = 3                                               // The maximum number of views to show.
    private val mAniDuration = 220L                                         // Animation run time.
    private val mViewSpace = Utils.convertDipToPixels(context, 22.0f) // The spacing of views.
    private var mShowIndex = 0

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

        this.mShowIndex = if(data.size > mMaxCount) mMaxCount else data.size

        removeAllViews()
        initAllView()
    }

    /**
     * Default view init.
     */
    private fun initAllView() {
        for (index in 0 until mShowIndex) {
            var position = mShowIndex - index - 1
            val scale = getScale(index)
            val margin = getMargin(position)
            Log.d("CardView", "fun init() index : $position, scale : $scale, margin : $margin")

            val view = mCardViewAdapter.getView(index, null, this)!!
            view.scaleX = scale
            view.scaleY = scale
            view.translationY = margin

            addViewInLayout(view, 0, getParams(view))
        }

//        2019-03-15 13:42:04.521 13213-13213/kr.kwonho87.cardview D/CardView: fun init() index : 2, scale : 1.0, margin : 0.0
//        2019-03-15 13:42:04.522 13213-13213/kr.kwonho87.cardview D/CardView: fun init() index : 1, scale : 0.93666667, margin : 58.0
//        2019-03-15 13:42:04.522 13213-13213/kr.kwonho87.cardview D/CardView: fun init() index : 0, scale : 0.87, margin : 116.0
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
                    addNewViewLast(getChildAt(0))

                    Log.d("CardView", "for childCount : $childCount")
//                    for(index in childCount - 1 downTo 0) {
                    for(index in 0 until childCount) {
                        var position = mShowIndex - index - 1

                        Log.d("CardView", "for index : $position")

                        // Get child view.
                        val view = getChildAt(index)
                        val scale = getScale(position)
                        val margin = getMargin(index)
                        Log.d("CardView", "fun anim() position : $position, scale : $scale, margin : $margin")

//                        // If it is the last child view.
//                        if (index == childCount - 1) {
//                            bringToTop(view)
//                        }
//                        else {
//                            view.animate()
//                                .translationY(margin)
//                                .setInterpolator(AccelerateInterpolator())
//                                .setListener(null)
//                                .scaleX(scale)
//                                .scaleY(scale)
//                                .duration = mAniDuration
//                        }

                        view.animate()
                            .translationY(margin)
                            .setInterpolator(AccelerateInterpolator())
                            .setListener(null)
                            .scaleX(scale)
                            .scaleY(scale)
                            .duration = mAniDuration
                    }

//                    // Repeat as many child views.
//                    for(index in 0 until childCount) {
//
//                        // Get child view.
//                        val view = getChildAt(childCount - index)
//                        val scale = getScale(index)
//                        val margin = getMargin(index)
//
//                        // If it is the last child view.
//                        if (index == childCount - 1) {
//                            bringToTop(view)
//                        }
//                        else {
//                            view.animate()
//                                .translationY(margin)
//                                .setInterpolator(AccelerateInterpolator())
//                                .setListener(null)
//                                .scaleX(scale)
//                                .scaleY(scale)
//                                .duration = mAniDuration
//                        }
//                    }

                    // Touch Unlock.
                    setIsEnabled(true)
                }
            })
    }

    /**
     * Add a new view to the 0th position.
     */
    private fun addNewViewLast(lastView: View) {
        var lastPosition = lastView.tag as Int
        var nextPosition = if((lastPosition + 1) > mCardViewAdapter.getData().size - 1) 0 else lastPosition + 1

        var view = mCardViewAdapter.getView(nextPosition, null, this)!!
        val scale = getScale(mShowIndex - 1)
        val margin = getMargin(0)
        Log.d("CardView", "fun addNewViewLast() nextPosition : $nextPosition, scale : $scale, margin : $margin")

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
            .alpha(1f).interpolator = AccelerateInterpolator()
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
            val scale = getScale(index)
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

        var lastView = getChildAt(0)
        removeView(lastView)

        var firstView = getChildAt(childCount - 1)
        var firstPosition = firstView.tag as Int - 1

        if(firstPosition < 0) {
            firstPosition = mCardViewAdapter.getData().size - 1
        }

        var newView = mCardViewAdapter.getView(firstPosition, null, this)!!
        newView.translationY = -2000f
        newView.animate()
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .duration = mAniDuration
        addView(newView)
    }

    /**
     *
     */
    private fun getScale(index: Int): Float {
        var scale = (mMaxCount - index - 1) / mMaxCount.toFloat() * 0.2f + 0.87f
//        Log.d("CardView", "scale : $scale, index : $index")
        return if(scale > 1.0f) return 1.0f else scale
    }

    /**
     *
     */
    private fun getMargin(index: Int): Float {
//        var margin = (index - 1) * mViewSpace
        var margin = mViewSpace * (mMaxCount - index - 1)
//        var margin = mViewSpace * index
//        Log.d("CardView", "margin : $margin, index : $index")

        return margin.toFloat()

//        return when(index) {
//            0 -> return 116f
//            1 -> return 58f
//            2 -> return 0f
//            else -> 0f
//        }
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