package kr.kwonho87.cardview.cardview.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
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

    // view 를 관리할 array
    private val mViewHolder = SparseArray<View>()

    // 뷰의 터치를 감지하여 계산하기 위한 변수.
    private val downX: Float = 0.toFloat()
    private var downY: Float = 0.toFloat()
    private var moveY: Float = 0.toFloat()

    // 최대 카드뷰 갯수.
    private var mIntMax = 0


    // 뷰들의 간격.
    private val VIEW_SPACE_VALUE = 22.0f
    private var VIEW_SPACE = Utils.convertDipToPixels(context, VIEW_SPACE_VALUE)

    // 최대 보여줄 뷰의 갯수.
    private val MAX_COUNT = 3

    // 애니메이션 동작시간.
    private val ANIMATION_DURATION = 220

    init {

        removeAllViews()

        setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val topView = getChildAt(childCount - 1)
                        moveY = topView.y - event.rawY
                        downY = event.y
                        mActionUp = true
                    }
                    MotionEvent.ACTION_MOVE -> {}
                    MotionEvent.ACTION_UP -> {mActionUp = true}
                }
                return super.onTouch(view, event)
            }
            override fun onSwipeUpDown(action: ACTION) {
                super.onSwipeUpDown(action)
                if (mIsDispatchTouch && mActionUp) {
                    when(action) {
                        ACTION.ACTION_UP -> {
                            goUp()
                        }
                        ACTION.ACTION_DOWN -> {
                            goDown()
                        }
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

    fun setData(data: ArrayList<String>) {
        mCardViewAdapter.setData(data)
        mCardViewAdapter.notifyDataSetChanged()

        removeAllViews()

        mDataSize = data.size

        initAllView()
    }

    /**
     * Default view init.
     */
    private fun initAllView() {

        var datas = mCardViewAdapter!!.getData()

        mIntMax = mCardViewAdapter!!.getData().size
        var adapterCount = mCardViewAdapter!!.getData().size
        if (adapterCount < MAX_COUNT) {
            mIntMax = mCardViewAdapter!!.getData().size
        } else {
            adapterCount = MAX_COUNT
        }

        mCardViewShowPosition = mIntMax
        Log.d(TAG, "initAllView : mCardViewShowPosition : $mCardViewShowPosition")
        Log.d(TAG, "initAllView : mIntMax : $mIntMax")

        val childCount = childCount
        if (childCount >= adapterCount) {
            return
        }

        for (index in 0 until adapterCount) {
            val convertView = mViewHolder.get(index)
            val view = mCardViewAdapter.getView(index, convertView, this)
            Log.d(TAG, "value : " + (view as ItemView).getValue())

            if (index != 0) {
                val scale = (MAX_COUNT - index - 1) / MAX_COUNT.toFloat() * 0.2f + 0.87f

                view.apply {
                    scaleX = scale
                    scaleY = scale
                    translationY = (index * VIEW_SPACE).toFloat()

                    if(index == MAX_COUNT - 1) {
                        alpha = 0.5f
                    }
                }
            }

            addViewInLayout(view, 0, getParams(view))

            mViewHolder.put(index, view)
        }
    }

    /**
     * Raise the first card up.
     */
    private fun goUp() {
        Log.d(TAG, "goUp : $mIsDispatchTouch")

        setAllViewShow(false)

        if (!mIsDispatchTouch) {
            return
        }

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
                        val scale = (MAX_COUNT - index) / MAX_COUNT.toFloat() * 0.2f + 0.87f

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

                        mViewHolder.put(childCount - nCount - 1, view)
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

        val lastPosition = getShowPosition(mCardViewShowPosition)
        Log.d(TAG, "lastPosition : $lastPosition")
        Log.d(TAG, "mCardViewShowPosition : $mCardViewShowPosition")

        val convertView = mViewHolder.get(0).apply {
            scaleX = 0.87f
            scaleY = 0.87f
            translationY = (VIEW_SPACE * (MAX_COUNT - 1)).toFloat()
        }

        val view = mCardViewAdapter!!.getView(lastPosition, convertView, this)

        addViewInLayout(view, 0, getParams(view!!))
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

        brindToDown()

        if (childCount > MAX_COUNT) {
            val bottomView = getChildAt(0)
            removeView(bottomView)
        } else if (mIntMax < MAX_COUNT) {
            val bottomView = getChildAt(0)
            removeView(bottomView)
        }

        for (nCount in 0 until childCount) {
            val view = getChildAt(nCount)
            val index = childCount - nCount
            val scaleX = (MAX_COUNT - index) / MAX_COUNT.toFloat() * 0.2f + 0.87f
            val topMargin = (index - 1) * VIEW_SPACE

            view.animate()
                .translationY(topMargin.toFloat())
                .setInterpolator(AccelerateInterpolator())
                .scaleX(scaleX)
                .scaleY(scaleX)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        Log.i(TAG, "onAnimationEnd")

                        mIsDispatchTouch = true
                    }
                })

            (view as ItemView).initFlipAnimation(mCardViewShowPosition)

            mViewHolder.put(childCount - nCount - 1, view)

            if (nCount == 0 && childCount == MAX_COUNT) {
                view.animate().alpha(0.5f)
            }
        }
    }

    /**
     * When you add a new view, the new down animation will work.
     */
    private fun brindToDown() {

        mCardViewShowPosition--
        Log.d(TAG, "mCardViewShowPosition-- : $mCardViewShowPosition")

        val convertView = mViewHolder.get(MAX_COUNT)
        val newView: View

        if (mIntMax == MAX_COUNT) {
            val nextPosition = getShowPosition(mCardViewShowPosition)
            Log.d(TAG, "nextPosition : $nextPosition")
            newView = mCardViewAdapter.getView(nextPosition, convertView, this)!!

            addViewInLayout(newView, childCount, getParams(newView))

            mViewHolder.put(0, newView)
        }
        else {
            val nextPosition = getShowPosition(mCardViewShowPosition)
            newView = mCardViewAdapter.getView(nextPosition, null, this)!!

            var params: FrameLayout.LayoutParams? = newView.layoutParams as FrameLayout.LayoutParams
            if (params == null) {
                params = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                params.gravity = Gravity.CENTER
            }

            addView(newView)

            mViewHolder.setValueAt(0, newView)
        }

        newView.translationY = -2000f
        newView.animate()
            .translationY(0f)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f).duration = ANIMATION_DURATION.toLong()
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

    /**
     * It calculates and returns the position value when sweeping up and down.
     */
    private fun getShowPosition(showPosition: Int): Int {
        var returnPosition = 0
        if (showPosition < 0) {
            returnPosition = Math.abs(showPosition) % mIntMax
            if (returnPosition != 0) {
                returnPosition = mIntMax - returnPosition
            }
        } else {
            returnPosition = showPosition % mIntMax
        }
        return returnPosition
    }
}