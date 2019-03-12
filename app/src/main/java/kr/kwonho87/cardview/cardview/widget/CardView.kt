package kr.kwonho87.cardview.cardview.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import kr.kwonho87.cardview.cardview.listener.OnSwipeTouchListener
import kr.kwonho87.cardview.cardview.item.ItemView
import kr.kwonho87.cardview.cardview.util.Utils

/**
 * kwonho87@gmail.com
 * 2019-03-12
 */
class CardView constructor(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val TAG = "CardView"

    private var mCardViewShowPosition = 0                   // The current position.
    private var mDataSize = 0                               // The total size of the data list to show.
    private var mIsDispatchTouch = true                     // Check whether animation works.
    private var mActionUp = true                            // Check TouchEvent ACTION_UP.
    private var mCardViewAdapter = CardViewAdapter(context) // Adapter.
    private var mIntMax = 0                                 // Maximum number of card views.
    private var mDatas = ArrayList<String>()

    private val mViewHolder = SparseArray<View>()           // view 를 관리할 array
    private val mMaxCount = 3                               // The maximum number of views to show.
    private val mAnimationDuration = 220L                   // Animation run time.

    private val mViewSpaceValue = 22.0f                                         // The spacing of views.
    private val mViewSpace = Utils.convertDipToPixels(context, mViewSpaceValue) // The spacing of views.

    private var downX: Float = 0.toFloat() // Variable to detect and calculate the touch of the view.
    private var downY: Float = 0.toFloat() // Variable to detect and calculate the touch of the view.
    private var moveY: Float = 0.toFloat() // Variable to detect and calculate the touch of the view.

    init {
        initLayout()
    }

    private fun initLayout() {
        removeAllViews()
        setOnTouchListener(onSwipeTouchListener) // Add Touch Listener.
    }

    private val onSwipeTouchListener: OnSwipeTouchListener = object : OnSwipeTouchListener(context) {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val topView = getChildAt(childCount - 1)
                    topView?.apply {
                        moveY = topView.y - event.rawY
                        downY = event.y
                    }

                    mActionUp = true
                }
                MotionEvent.ACTION_MOVE -> {}
                MotionEvent.ACTION_UP -> {mActionUp = true}
            }
            return super.onTouch(view, event)
        }

        override fun onSwipeUp() {
            super.onSwipeUp()

            // 위로 올리는 경우. mShowPosition 값이 0이 아닌경우.
            if (mCardViewAdapter!!.count > 1 && mIsDispatchTouch && mActionUp) {
                goUp()
                mActionUp = false
            }
        }

        override fun onSwipeDown() {
            super.onSwipeDown()

            // 아래로 내리는 경우. mShowPosition 값이 adapter의 갯수보다 크지 않는 경우.
            if (mCardViewAdapter!!.count > 1 && mIsDispatchTouch && mActionUp) {
                goDown()
                mActionUp = false
            }
        }

        override fun onClick() {
            super.onClick()

            val view = getChildAt(childCount - 1) as ItemView
            if (view != null) {
                view!!.onClick()
            }
        }
    }

    /**
     *
     */
    fun setData(data: ArrayList<String>) {
        mDatas = data
        mDataSize = data.size

        mCardViewAdapter.run {
            setData(data)
            notifyDataSetChanged()
        }

        removeAllViews()

        initAllView(data)

        for (nCount in 0 until childCount) {
            val view = getChildAt(nCount)
            view.setOnTouchListener(onSwipeTouchListener)
        }
    }

    /**
     * Initialize the screen.
     * Creates the view by mMaxCount.
     */
    private fun initAllView(data: ArrayList<String>) {
        //        LogUtil.d(TAG, "initAllView");
        if (mCardViewAdapter != null) {
            mIntMax = mCardViewAdapter.getData().size
            var adapterCount = mCardViewAdapter.getData().size
            if (adapterCount < mMaxCount) {
                mIntMax = mCardViewAdapter.getData().size
            } else {
                adapterCount = mMaxCount
            }

            mCardViewShowPosition = mIntMax
            Log.d(TAG, "initAllView : mCardViewShowPosition : $mCardViewShowPosition")
            Log.d(TAG, "initAllView : mIntMax : $mIntMax")

            val childCount = childCount
            if (childCount >= adapterCount) {
                return
            }

            for (nCount in 0 until adapterCount) {

                // 뷰생성.
                val convertView = mViewHolder.get(nCount)
                val view = mCardViewAdapter.getView(nCount, convertView, this)

                // 0번째 뷰가 아닌 경우에만 스케일을 조절한다.
                if (nCount != 0) {
                    val scale = (mMaxCount - nCount - 1) / mMaxCount.toFloat() * 0.2f + 0.87f
                    //                    LogUtil.d(TAG, "scale : " + scale);

                    // 스케일을 조정한다.
                    view!!.scaleX = scale
                    view!!.scaleY = scale

                    // y축으로 이동시킨다.
                    view!!.translationY = (nCount * mViewSpace).toFloat()
                }

                //                LogUtil.d("mCardViewAdapter: " + mCardViewAdapter + " / view: " + view);
                // params 생성.
                var params: FrameLayout.LayoutParams? = view!!.getLayoutParams() as FrameLayout.LayoutParams
                if (params == null) {
                    params = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    params.gravity = Gravity.CENTER
                }

                // 만약 3번째 뷰인 경우 투명도를 50%로 한다.
                if (nCount == mMaxCount - 1) {
                    view!!.alpha = 0.5f
                }

                // 뷰를 add한다.
                addViewInLayout(view, 0, params)

                // holder에 추가한다.
                mViewHolder.put(nCount, view)
            }
        }
    }

    /**
     * @return
     * @author kwonho87
     * @date 2015. 5. 26. 오전 11:24:17
     */
    private// 뷰 상태가 false이면 그냥 리턴한다.
    // 제일 위의 뷰를 가져온다.
    // topView.getHitRect(topRect); 在4.3以前有bug，用以下方法代替
    // 如果按下的位置不在顶部视图上，则不移动
    //        Log.i(TAG, "isViewShow : " + bState);
    val isViewShow: Boolean
        get() {

            var bState = true
            if (!isEnabled) {
                bState = false
            }
            val topView = getChildAt(childCount - 1)
            var topRect = Rect()
            topRect = getHitRect(topRect, topView)
            if (!topRect.contains(downX.toInt(), downY.toInt())) {
                bState = false
            }
            return bState
        }

//    @Throws(Throwable::class)
//    protected override fun finalize() {
//        Log.i(TAG, "finalize")
//        removeAllViews()
//        super.finalize()
//    }

    /**
     * 터치리스너 등록.
     * @param listener
     */
    fun setOnSwipeTouchListener(listener: OnSwipeTouchListener) {
        setOnTouchListener(listener)
    }

    /**
     * 맨 위의 뷰를 y좌표로 이동시킨다.
     * @param event
     */
    private fun moveView(event: MotionEvent) {

        if (mCardViewAdapter!!.getCount() > 1 && mIsDispatchTouch && mActionUp) {
            val topView = getChildAt(childCount - 1)

            //            Rect rect = new Rect();
            //            topView.getHitRect(rect);
            //            LogUtil.i(TAG, "rect.top : " + rect.top);

            topView.animate().y(event.rawY + moveY).setDuration(0).start()
            //            topView.animate().y(event.getRawY()).setDuration(0).start();

        }
    }

    private fun moveViewDefault(event: MotionEvent) {
        val topView = getChildAt(childCount - 1)
        //        Log.d(TAG, "topView.getY : " + topView.getY());
        //        Log.d(TAG, "event.getY : " + event.getY());
        //        Log.d(TAG, "event.getRawY : " + event.getRawY());

        //        Rect rect = new Rect();
        //        topView.getHitRect(rect);
        //        LogUtil.d(TAG, "rect.top : " + rect.top);

        //        Rect rect = new Rect();
        //        topView.getHitRect(rect);
        //        LogUtil.d(TAG, "rect.top : " + rect.top);
        //        LogUtil.d(TAG, "rect.bottom : " + rect.bottom);
        //        LogUtil.d(TAG, "topView.getHeight() : " + topView.getHeight());


        // 아래로 내리는 경우. mShowPosition 값이 adapter의 갯수보다 크지 않는 경우.
        if (mCardViewAdapter!!.getCount() > 1 && mIsDispatchTouch && mActionUp) {
            val getY = topView.y
            val eventGetRawY = Math.abs(event.rawY)
            val eventGetY = Math.abs(event.y)


            var value = Math.abs(getY) - eventGetRawY
            value = Math.abs(value)
            //            Log.i(TAG, "value : " + value);
            if (getY < 0.0f && value > 200.0f) {

                goUp()

                mActionUp = false

                //                // 상하 플리킹 시 최근길찾기 리스트뷰를 숨겨주기 위한 콜백.
                //                if (mOnSlidePageChangeListener != null) {
                //                    mOnSlidePageChangeListener.onPageSelected(-1);
                //                }
            } else {
                val animator = topView.animate()
                animator.y(0f)
                animator.duration = mAnimationDuration.toLong()
                animator.interpolator = OvershootInterpolator()
                animator.start()
            }
        }
    }

    /**
     * @param ev
     * @return
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (mIsDispatchTouch) {
            super.dispatchTouchEvent(ev)
        } else {
            false
        }
    }

    /**
     * 화면을 업데이트 한다.
     */
    fun notifyDataSetChanged() {
        if (mCardViewAdapter != null) {
            mCardViewAdapter!!.notifyDataSetChanged()
        }
    }


    fun refresh() {
        val topView = getChildAt(childCount - 1) as ItemView
        //        topView.resetView();
    }

    /**
     * 제일 앞장의 카드를 위로 올린다.
     *
     * @author kwonho87
     * @date 2015. 5. 20. 오후 5:51:48
     */
    private fun goUp() {
        //        Log.d(TAG, "mCardViewShowPosition : " + mCardViewShowPosition);

        //        // 뷰 상태가 false이면 그냥 리턴한다.
        //        if (!isViewShow()) {
        //            return;
        //        }

        // 뷰의 상태를 false로 설정한다.
        setAllViewShow(false)

        // 터치가 false이면 그냥 리턴한다.
        if (!mIsDispatchTouch) {
            return
        }

        // 터치 false.
        mIsDispatchTouch = false

        //        // 현재 보여지는 포지션을 증가시킨다.
        //        mShowPosition++;

        // 제일 위의 뷰를 가져온다.
        val topView = getChildAt(childCount - 1)

        // 이동시킬 y좌표 값을 계산한다.
        val transY = topView.translationY + topView.height
        //        Log.i(TAG, "transY : " + transY);

        // 제일 위의 뷰를 화면밖으로 이동시킨다.
        topView.animate()
            .translationY(-transY)
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(mAnimationDuration)
            .setInterpolator(AccelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    //                        Log.i(TAG, "onAnimationEnd");

                    // 애니메이션이 종료되면 맨위뷰를 제거한다.
                    removeView(topView)

                    // 뷰들을 세팅한다.
                    addNewViewLast()

                    // 현재 보여지는 포지션을 증가시킨다.
                    mCardViewShowPosition++

                    // 차일드 갯수만큼 반복한다.
                    for (nCount in 0 until childCount) {

                        // 뷰를 가져온다.
                        val view = getChildAt(nCount)

                        // 스케일을 계산하기 위한 index 값을 구한다.
                        val index = childCount - nCount

                        // 축소시킬 스케일을 계산한다.
                        val scale = (mMaxCount - index) / mMaxCount.toFloat() * 0.2f + 0.87f

                        // 제일 앞에 있는 뷰르 위로 이동시킨다.
                        if (nCount == childCount - 1) {

                            // 뷰를 맨앞으로 이동시킨다.
                            bringToTop(view)

                        } else {

                            // 마진계산
                            val margin = (index - 1) * mViewSpace

                            if (nCount == 0 && childCount > 2) {

                                // 나머지 뷰들을 한단계 위로 이동시킨다.
                                view.animate()
                                    .translationY(margin.toFloat())
                                    .setInterpolator(AccelerateInterpolator())
                                    .setListener(null)
                                    .scaleX(scale)
                                    .scaleY(scale)
                                    .alpha(0.5f).duration = mAnimationDuration
                            } else {

                                // 나머지 뷰들을 한단계 위로 이동시킨다.
                                view.animate()
                                    .translationY(margin.toFloat())
                                    .setInterpolator(AccelerateInterpolator())
                                    .setListener(null)
                                    .scaleX(scale)
                                    .scaleY(scale)
                                    .alpha(1f).duration = mAnimationDuration
                            }
                        }

                        // 뷰 플립애니메이션 초기화 및 카드뷰 포지션 세팅.
                        (view as ItemView).initFlipAnimation(mCardViewShowPosition)

                        // holder에 추가한다.
                        mViewHolder.put(childCount - nCount - 1, view)
                    }

                    // 뷰의 상태를 true로 설정한다.
                    setAllViewShow(true)

                    // 터치 true.
                    mIsDispatchTouch = true
                }
            })
    }

    /**
     * 마지막 위치에 새로운 뷰를 추가한다.
     *
     * @author kwonho87
     * @date 2015. 5. 26. 오후 4:40:33
     */
    private fun addNewViewLast() {
        //        if (mShowPosition <= mCardViewAdapter.getCount() - MAX_COUNT) {
        //            int lastPosition = ((BeaconContentView) mViewHolder.get(mViewHolder.size() - 1)).getPosition();

        //            lastPosition = mShowPosition % MAX_COUNT;
        val lastPosition = getShowPosition(mCardViewShowPosition)

        val convertView = mViewHolder.get(0)
        val view = mCardViewAdapter!!.getView(lastPosition, convertView, this)

        // 뷰를 add한다.
        var params: FrameLayout.LayoutParams? = view?.layoutParams as LayoutParams?
        if (params == null) {
            params =
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            params.gravity = Gravity.CENTER
        }

        // 스케일 계산.
        val scale = (mMaxCount - mMaxCount) / mMaxCount.toFloat() * 0.2f + 0.87f

        // 뷰의 스케일을 조정한다.
        convertView.scaleX = scale
        convertView.scaleY = scale
        convertView.translationY = (mViewSpace * (mMaxCount - 1)).toFloat()

        // 뷰를 추가한다.
        addViewInLayout(view, 0, params)
        //        }
    }

    /**
     * 뷰를 맨앞으로 이동시킨다.
     *
     * @param view
     */
    private fun bringToTop(view: View?) {
        if (view != null) {
            view.animate()
                .translationY(0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(mAnimationDuration)
                .alpha(1f).interpolator = AccelerateInterpolator()
        }
    }

    /**
     * 올라간 카드를 다시 내려서 가져온다.
     *
     * @author kwonho87
     * @date 2015. 5. 20. 오후 5:52:04
     */
    private fun goDown() {

        // 터치가 false이면 그냥 리턴한다.
        if (!mIsDispatchTouch) {
            return
        }

        // 터치 false.
        mIsDispatchTouch = false

        // 이전 뷰를 새로 생성하여 내려오면서 보여준다.
        brindToDown()

        // 차일드 뷰의 갯수가 최대 보여질 뷰의 갯수와 같은 경우에만 제일 마지막 뷰는 제거한다.
        if (childCount > mMaxCount) {
            val bottomView = getChildAt(0)
            removeView(bottomView)
        } else if (mIntMax < mMaxCount) {
            val bottomView = getChildAt(0)
            removeView(bottomView)
        }

        // 뷰들을 모두 아래로 이동시킨다.
        for (nCount in 0 until childCount) {

            // 뷰를 가져온다.
            val view = getChildAt(nCount)

            // 스케일을 계산하기 위한 index 값을 구한다.
            val index = childCount - nCount

            // 축소시킬 스케일을 계산한다.
            val scaleX = (mMaxCount - index) / mMaxCount.toFloat() * 0.2f + 0.87f

            // 이동시킬 마진을 계산한다.
            val topMargin = (index - 1) * mViewSpace
            //            Log.d(TAG, "topMargin : " + topMargin);

            // 뷰를 이동시킨다.
            view.animate()
                .translationY(topMargin.toFloat())
                .setInterpolator(AccelerateInterpolator())
                .scaleX(scaleX)
                .scaleY(scaleX)
                .alpha(1f)
                .setDuration(mAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        Log.i(TAG, "onAnimationEnd")

                        // 터치 true
                        mIsDispatchTouch = true
                    }
                })

            // 새로 추가할 뷰의 퀵버튼을 초기화 한다.
            (view as ItemView).initFlipAnimation(mCardViewShowPosition)

            // holder에 추가한다.
            mViewHolder.put(childCount - nCount - 1, view)

            // 만약 차일드뷰가 3개인 경우 마지막 뷰는 투명도를 적용한다.
            if (nCount == 0 && childCount == mMaxCount) {
                view.animate().alpha(0.5f)
            }
        }
    }

    /**
     * 뷰를 새로 추가하면새 아래로 내려오는 애니메이션을 동작한다.
     *
     * @author kwonho87
     * @date 2015. 5. 25. 오후 7:27:33
     */
    private fun brindToDown() {

        // 현재 보여지는 포지션을 감소시킨다.
        mCardViewShowPosition--
        Log.d(TAG, "mCardViewShowPosition-- : $mCardViewShowPosition")

        // 마지막 뷰를 가져온다.
        val convertView = mViewHolder.get(mMaxCount)

        // 새로 추가할 뷰
        val newView: View

        // 만약 차일드갯수가 3개인 경우
        if (mIntMax == mMaxCount) {

            // holder에서 가져온 뷰를 재사용한다.
            //            int nextPosition = mCardViewShowPosition % MAX_COUNT;
            val nextPosition = getShowPosition(mCardViewShowPosition)
            Log.d(TAG, "nextPosition : $nextPosition")
            newView = mCardViewAdapter!!.getView(nextPosition, convertView, this)!!

            // params 생성
            var params: FrameLayout.LayoutParams? = newView.layoutParams as FrameLayout.LayoutParams
            if (params == null) {
                params = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                params.gravity = Gravity.CENTER
            }

            // 뷰를 추가한다.
            addViewInLayout(newView, childCount, params)

            // holder에 추가한다.
            mViewHolder.put(0, newView)
        } else {

            // 새로 뷰를 생성한다.
            val nextPosition = getShowPosition(mCardViewShowPosition)
            newView = mCardViewAdapter!!.getView(nextPosition, null, this)!!

            // params 생성
            var params: FrameLayout.LayoutParams? = newView.layoutParams as FrameLayout.LayoutParams
            if (params == null) {
                params = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                params.gravity = Gravity.CENTER
            }

            // 뷰를 추가한다.
            addView(newView)

            // holder에 추가한다.
            mViewHolder.setValueAt(0, newView)
        }// 차일드갯수가 3개가 아닌경우

        // 뷰를 y축 화면 밖에서 0으로 이동시킨다.
        newView.translationY = -2000f

        // 뷰를 y축 0좌표로 이동시킨다.
        newView.animate()
            .translationY(0f)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f).duration = mAnimationDuration
    }

    /**
     * 차일드뷰를 포함한 모든 뷰의 enable/disable 세팅을 한다.
     *
     * @param state
     */
    private fun setAllViewShow(state: Boolean) {
        for (nCount in 0 until childCount) {
            val view = getChildAt(nCount)
            view.isEnabled = state
        }
        isEnabled = state
    }

    /**
     * @param rect
     * @param child
     * @return
     */
    private fun getHitRect(rect: Rect, child: View): Rect {
        rect.left = child.left
        rect.right = child.right
        rect.top = (child.top + child.translationY).toInt()
        rect.bottom = (child.bottom + child.translationY).toInt()
        return rect
    }

    /**
     * 상하 스크롤 시 포지션값을 계산해서 리턴한다.
     * @param showPosition
     * @return
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

    companion object {

    }
}