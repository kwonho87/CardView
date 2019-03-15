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
class CardView constructor(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var mCardViewAdapter = CardViewAdapter(context)                 // Adapter.
    private val MAX_COUNT = 3                                               // The maximum number of views to show.
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
        mCardViewAdapter.setMaxCount(MAX_COUNT)

        this.mShowIndex = if(data.size > MAX_COUNT) MAX_COUNT else data.size

        removeAllViews()
        initAllView()
    }

    /**
     * Default view init.
     */
    private fun initAllView() {
        for (index in 0 until mShowIndex) {
            val view = mCardViewAdapter.getView(index, null, this)!!
            view.scaleX = getScale(index)
            view.scaleY = getScale(index)
            view.translationY = getMargin(mShowIndex - index - 1)

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
                    addNewViewLast(getChildAt(0))

                    // Get child view.
                    for(index in 0 until childCount) {
                        val position = mShowIndex - index - 1
                        val view = getChildAt(index)
                        view.animate()
                            .setInterpolator(AccelerateInterpolator())
                            .setListener(null)
                            .scaleX(getScale(position))
                            .scaleY(getScale(position))
                            .translationY(getMargin(index))
                            .duration = mAniDuration
                    }

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
        view.scaleX = getScale(mShowIndex - 1)
        view.scaleY = getScale(mShowIndex - 1)
        view.translationY = getMargin(0)
        addViewInLayout(view, 0, getParams(view))
    }

    /**
     * Bring the card up again.
     */
    private fun goDown() {

        // Touch Lock.
        setIsEnabled(false)

        // Add a new view and start down animation.
        addNewViewFirst()

        // Get child view.
        for(index in 0 until childCount) {
            val position = mShowIndex - index - 1
            val view = getChildAt(index)
            view.animate()
                .setInterpolator(AccelerateInterpolator())
                .setListener(null)
                .scaleX(getScale(position))
                .scaleY(getScale(position))
                .translationY(getMargin(index))
                .setDuration(mAniDuration)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {

                        // Touch Unlock.
                        setIsEnabled(true)
                    }
                })
        }
    }

    /**
     *
     */
    private fun addNewViewFirst() {
        var lastView = getChildAt(0)
        removeView(lastView)

        var firstView = getChildAt(childCount - 1)
        var firstPosition = firstView.tag as Int
        var prevPosition = if(firstPosition - 1 < 0) mCardViewAdapter.getData().size -1 else firstPosition - 1

        var newView = mCardViewAdapter.getView(prevPosition, null, this)!!
        newView.scaleX = 1f
        newView.scaleY = 1f
        newView.translationY = -2000f
        addView(newView)
    }

    /**
     *
     */
    private fun getScale(index: Int): Float {
        var scale = (MAX_COUNT - index - 1) / MAX_COUNT.toFloat() * 0.2f + 0.87f
        return if(scale > 1.0f) return 1.0f else scale
    }

    /**
     *
     */
    private fun getMargin(index: Int): Float {
        var margin = mViewSpace * (MAX_COUNT - index - 1)
        return margin.toFloat()
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