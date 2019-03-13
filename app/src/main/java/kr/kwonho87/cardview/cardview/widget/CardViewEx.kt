package kr.kwonho87.cardview.cardview.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
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

    // TAG
    private val TAG = "CardView"

    // 현재 보여지는 포지션.
    private var mCardViewShowPosition = 0

    // 보여줄 데이터 리스트의 전체 사이즈
    private var mDataSize = 0

    // 애니메이션 동작여부 체크.
    private var mIsDispatchTouch = true

    // TouchEvent ACTION_UP 여부 체크.
    private var mActionUp = true

    // adapter
    private var mCardViewAdapter = CardViewAdapter(context)

//    // view 를 관리할 array
//    private val mViewHolder = SparseArray<View>()

    // 최대 카드뷰 갯수.
    private var mIntMax = 0


    // 뷰들의 간격.
    private val VIEW_SPACE_VALUE = 22.0f
    private var VIEW_SPACE = Utils.convertDipToPixels(context, VIEW_SPACE_VALUE)

    // 최대 보여줄 뷰의 갯수.
    private val mMaxCount = 3

    // 애니메이션 동작시간.
    private val ANIMATION_DURATION = 220

    /**
     * Init.
     */
    init {

        setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> mActionUp = true
                    MotionEvent.ACTION_UP -> mActionUp = true
                }
                return super.onTouch(view, event)
            }
            override fun onSwipeUpDown(action: ACTION) {
                super.onSwipeUpDown(action)
                if (mIsDispatchTouch && mActionUp) {
                    when(action) {
                        ACTION.ACTION_UP -> goUp()
                        ACTION.ACTION_DOWN -> goDown()
                    }
                    mActionUp = false
                }
            }
            override fun onClick() {
                super.onClick()
                Log.d(TAG, "onClick")
            }
        })
    }

    /**
     * Set data.
     */
    fun setData(data: LinkedHashMap<Int, String>) {
        mDataSize = data.size
        mCardViewAdapter.setData(data)
        mCardViewAdapter.setMaxCount(mMaxCount)
        mCardViewAdapter.notifyDataSetChanged()

        removeAllViews()
        initAllView()
    }

    /**
     * Default view init.
     */
    private fun initAllView() {
        var data = mCardViewAdapter.getData()

        var createViewCount =
            if(data.size > mMaxCount) {
                mMaxCount
            }
            else {
                data.size
            }

        for (position in 0 until createViewCount) {
            val view = mCardViewAdapter.getView(position, null, this) as ItemView
            view.scaleX = 1 - (position % 0.93f)
            view.scaleY = 1 - (position % 0.93f)
            view.translationY = (position * VIEW_SPACE).toFloat()

            addViewInLayout(view, 0, getParams(view))
        }
    }

    /**
     * Raise the first card up.
     */
    private fun goUp() {
        Log.d(TAG, "goUp : $mIsDispatchTouch")

        setAllViewShow(false)

        if (!mIsDispatchTouch) {return}
        mIsDispatchTouch = false

        val topView = getChildAt(childCount - 1)
        topView.animate()
            .translationY(-(topView.translationY + topView.height))
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(ANIMATION_DURATION.toLong())
            .setInterpolator(AccelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    removeView(topView)

                    addNewViewLast()

                    mCardViewShowPosition++
                    Log.d(TAG, "mCardViewShowPosition++ : $mCardViewShowPosition")

                    for (nCount in 0 until childCount) {
                        val view = getChildAt(nCount)
                        val index = childCount - nCount
                        val scale = (mMaxCount - index) / mMaxCount.toFloat() * 0.2f + 0.87f

                        if (nCount == childCount - 1) {
                            bringToTop(view)

                        }
                        else {
                            val margin = (index - 1) * VIEW_SPACE

                            if (nCount == 0 && childCount > 2) {
                                view.animate()
                                    .translationY(margin.toFloat())
                                    .setInterpolator(AccelerateInterpolator())
                                    .setListener(null)
                                    .scaleX(scale)
                                    .scaleY(scale)
                                    .alpha(0.5f).duration = ANIMATION_DURATION.toLong()
                            }
                            else {
                                view.animate()
                                    .translationY(margin.toFloat())
                                    .setInterpolator(AccelerateInterpolator())
                                    .setListener(null)
                                    .scaleX(scale)
                                    .scaleY(scale)
                                    .alpha(1f).duration = ANIMATION_DURATION.toLong()
                            }
                        }

                        (view as ItemView).initFlipAnimation(mCardViewShowPosition)
                    }

                    setAllViewShow(true)

                    mIsDispatchTouch = true
                }
            })
    }

    /**
     * Add a new view to the 0th position.
     */
    private fun addNewViewLast() {
        var value = (getChildAt(0) as ItemView).getValue()
        var data = mCardViewAdapter.getData()

        var position = 0
        for (index in 0 until data.size) {
            if(value == data[index]) {
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
        view.translationY = (VIEW_SPACE * (mMaxCount - 1)).toFloat()
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
            .setDuration(ANIMATION_DURATION.toLong())
            .alpha(1f).interpolator = AccelerateInterpolator()
    }

    /**
     * Bring the card up again.
     */
    private fun goDown() {
        Log.d(TAG, "goDown : $mIsDispatchTouch")

        if (!mIsDispatchTouch) {
            return
        }

        mIsDispatchTouch = false

        bringToDown()

        for (nCount in 0 until childCount) {
            val view = getChildAt(nCount)
            val index = childCount - nCount
//            val scaleX = (mMaxCount - index) / mMaxCount.toFloat() * 0.2f + 0.87f
            val topMargin = (index - 1) * VIEW_SPACE

            view.animate()
                .translationY(topMargin.toFloat())
                .setInterpolator(AccelerateInterpolator())
                .scaleX(getScaleX(index))
                .scaleY(getScaleX(index))
                .alpha(1f)
                .setDuration(ANIMATION_DURATION.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        Log.i(TAG, "onAnimationEnd")

                        mIsDispatchTouch = true
                    }
                })

            (view as ItemView).initFlipAnimation(mCardViewShowPosition)

//            mViewHolder.put(childCount - nCount - 1, view)

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
        Log.d(TAG, "frontValue : $frontValue")

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
        Log.d(TAG, "position : $position")

        view.setData(position, mCardViewAdapter.getData()[position].toString())
        view.translationY = -2000f
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f).duration = ANIMATION_DURATION.toLong()
        addView(view)



//        mCardViewShowPosition--
//        Log.d(TAG, "mCardViewShowPosition-- : $mCardViewShowPosition")
//
////        val convertView = mViewHolder.get(COUNT)
//        val newView: View
//
//        if (mIntMax == mMaxCount) {
//            val nextPosition = getShowPosition(mCardViewShowPosition)
//            Log.d(TAG, "nextPosition : $nextPosition")
//            newView = mCardViewAdapter.getView(nextPosition, null, this)!!
//
//            addViewInLayout(newView, childCount, getParams(newView))
//
////            mViewHolder.put(0, newView)
//        }
//        else {
//            val nextPosition = getShowPosition(mCardViewShowPosition)
//            newView = mCardViewAdapter.getView(nextPosition, null, this)!!
//
//            var params: FrameLayout.LayoutParams? = newView.layoutParams as FrameLayout.LayoutParams
//            if (params == null) {
//                params = FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.MATCH_PARENT
//                )
//                params.gravity = Gravity.CENTER
//            }
//
//            addView(newView)
//
////            mViewHolder.setValueAt(0, newView)
//        }
//
//        newView.translationY = -2000f
//        newView.animate()
//            .translationY(0f)
//            .alpha(1f)
//            .scaleX(1f)
//            .scaleY(1f).duration = ANIMATION_DURATION.toLong()
    }

    /**
     * Get scale value.
     */
    private fun getScaleX(index: Int): Float {
        return (mMaxCount - index) / mMaxCount.toFloat() * 0.2f + 0.87f
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
    private fun setAllViewShow(state: Boolean) {
        for (nCount in 0 until childCount) {
            val view = getChildAt(nCount)
            view.isEnabled = state
        }
        isEnabled = state
    }
}