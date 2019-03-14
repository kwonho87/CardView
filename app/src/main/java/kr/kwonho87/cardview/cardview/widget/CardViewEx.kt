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
class CardViewEx constructor(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

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
    fun setData(data: LinkedHashMap<Int, String>) {
        mCardViewAdapter.setData(data)
        mCardViewAdapter.setMaxCount(mMaxCount)
        mCardViewAdapter.notifyDataSetChanged()

        removeAllViews()
        initAllView(data)
    }

    /**
     * Default view init.
     */
    private fun initAllView(data: LinkedHashMap<Int, String>) {
        var maxViewCount = if(data.size > mMaxCount) mMaxCount else data.size

        for (index in 0 until maxViewCount) {
            val view = mCardViewAdapter.getView(index, null, this) as ItemView
            view.scaleX = 1 - (index % 0.93f)
            view.scaleY = 1 - (index % 0.93f)
            view.translationY = getTranslation(index)

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

                    for(index in 0 until childCount) {
                        val view = getChildAt(index)
                        val scale = getScale(index)

                        if (index == childCount - 1) {
                            bringToTop(view)

                        }
                        else {
                            val margin = getMargin(index)
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
        var data = mCardViewAdapter.getData()

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

        var view = mCardViewAdapter.getView(position, null, this)!!
        view.scaleX = 0.87f
        view.scaleY = 0.87f
        view.translationY = (mViewSpace * (mMaxCount - 1)).toFloat()
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
        setIsEnabled(false)

        bringToDown()

        for (nCount in 0 until childCount) {
            val view = getChildAt(nCount)
            val index = childCount - nCount
            val topMargin = (index - 1) * mViewSpace

            view.animate()
                .translationY(topMargin.toFloat())
                .setInterpolator(AccelerateInterpolator())
                .scaleX(getScaleXY(index))
                .scaleY(getScaleXY(index))
                .alpha(1f)
                .setDuration(mAniDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        setIsEnabled(true)
                    }
                })


            if (nCount == 0 && childCount == mMaxCount) {
                view.animate().alpha(0.5f)
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
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f).duration = mAniDuration
        addView(view)
    }

    /**
     * Get scale value.
     */
    private fun getScaleXY(index: Int): Float {
        var value = (mMaxCount - index) / mMaxCount.toFloat() * 0.2f + 0.93f
        Log.d("CardView", "index : $index")
        Log.d("CardView", "getScaleXY : $value")

        return value
    }

    private fun getScale(index: Int): Float {
        return when(index) {
            0 -> return 0.87f
            1 -> return 0.93f
            2 -> return 1.0f
            else -> 0f
        }
    }

    private fun getMargin(index: Int): Float {
        return when(index) {
            0 -> return 116f
            1 -> return 58f
            2 -> return 0f
            else -> 0f
        }
    }

    /**
     * Get translationY
     */
    private fun getTranslation(index: Int): Float {
        return (index * mViewSpace).toFloat()
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
        return getChildAt(0).isEnabled
    }
}