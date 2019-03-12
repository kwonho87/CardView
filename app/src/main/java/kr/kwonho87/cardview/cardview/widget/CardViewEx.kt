package kr.kwonho87.cardview.cardview.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
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
import kr.kwonho87.cardview.cardview.item.ItemView
import kr.kwonho87.cardview.cardview.listener.OnSwipeTouchListener
import kr.kwonho87.cardview.cardview.util.Utils


/**
 * 카드를 최대 갯수까지 겹쳐서 보여주는 위젯.
 * 위, 아래 스와이프를 통해 뷰 이동이 가능하다.
 * @author kwonho87
 * @since 2015-05-28 오후 3:03
 */
@TargetApi(12)
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

        // 먼저 모든 뷰들을 제거한다.
        removeAllViews()

        // 터치리스너.
        setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val topView = getChildAt(childCount - 1)
                        if (topView != null) {
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
                if (mCardViewAdapter!!.getCount() > 1 && mIsDispatchTouch && mActionUp) {
                    goUp()
                    mActionUp = false
                }
            }

            override fun onSwipeDown() {
                super.onSwipeDown()

                // 아래로 내리는 경우. mShowPosition 값이 adapter의 갯수보다 크지 않는 경우.
                if (mCardViewAdapter!!.getCount() > 1 && mIsDispatchTouch && mActionUp) {
                    goDown()
                    mActionUp = false
                }
            }

            override fun onClick() {
                super.onClick()
                Log.d(TAG, "onClick")

//            // 클릭.
//            val view = getChildAt(childCount - 1) as ItemView
//            if (view != null) {
//                view!!.onClick()
//            }
            }
        })
    }

    /**
     * 어댑터에 있는 데이터를 가져온다.
     * @return
     */
    /**
     * 데이터 세팅.
     *
     * @param data
     */

    fun setData(data: ArrayList<String>) {
        if (mCardViewAdapter != null) {
            mCardViewAdapter!!.setData(data)
            mCardViewAdapter!!.notifyDataSetChanged()
        }

        // 먼저 모든 뷰들을 제거한다.
        removeAllViews()

        // 보여줄 전체 데이터의 사이즈를 저장한다.
        mDataSize = data.size

        // 뷰들을 세팅한다.
        initAllView()

//        for (nCount in 0 until childCount) {
//            val view = getChildAt(nCount)
//            view.setOnTouchListener(onSwipeTouchListener)
//        }
    }

//    /**
//     * 생성자
//     *
//     * @param context
//     */
//    constructor(context: Context) : super(context) {
//
//        // init
//        init(context)
//    }
//
//    /**
//     * 생성자
//     *
//     * @param context
//     * @param attrs
//     */
//    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
//
//        // init
//        init(context)
//    }

//    /**
//     * init
//     *
//     * @param context
//     * @author kwonho87
//     * @date 2015. 5. 20. 오후 5:13:29
//     */
//    private fun init(context: Context) {
//        VIEW_SPACE = Utils.convertDipToPixels(context, VIEW_SPACE_VALUE)
//
//        // 어댑터 초기화.
//        mCardViewAdapter = CardViewAdapter(context)
//
//        // 먼저 모든 뷰들을 제거한다.
//        removeAllViews()
//
//        // 터치리스너.
//        setOnTouchListener(onSwipeTouchListener)
//    }

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
     * 화면 초기화.
     *
     * @author kwonho87
     * @date 2015. 5. 26. 오후 4:37:59
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

            // 뷰생성.
            val convertView = mViewHolder.get(index)
            val view = mCardViewAdapter!!.getView(index, convertView, this)

            Log.d(TAG, "value : " + (view as ItemView).getValue())

            // Scale only if it is not the 0th view.
            if (index != 0) {
                val scale = (MAX_COUNT - index - 1) / MAX_COUNT.toFloat() * 0.2f + 0.87f

                view.apply {

                    // Adjust the scale.
                    scaleX = scale
                    scaleY = scale

                    // Move in the y position
                    translationY = (index * VIEW_SPACE).toFloat()

                    // If it is the third view, the transparency is set to 50%.
                    if(index == MAX_COUNT - 1) {
                        alpha = 0.5f
                    }
                }
            }

            // Add child view.
            addViewInLayout(view, 0, getParams(view))

            // holder에 추가한다.
            mViewHolder.put(index, view)
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
     * Raise the first card up.
     */
    private fun goUp() {
        Log.d(TAG, "goUp : $mIsDispatchTouch")

        // Set the state of the view to false.
        setAllViewShow(false)

        // If the touch is false, it just returns.
        if (!mIsDispatchTouch) {
            return
        }

        // Touch false. Prevent duplicate touches.
        mIsDispatchTouch = false

        // Get the top view.
        val topView = getChildAt(childCount - 1)

        // Calculate the y-coordinate value to move. So the card moves out of the screen.
        val transY = topView.translationY + topView.height

        // Move the top view out of the screen.
        topView.animate()
            .translationY(-transY)
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(ANIMATION_DURATION.toLong())
            .setInterpolator(AccelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    // When the animation ends, remove the top view.
                    removeView(topView)

                    // Set new views.
                    addNewViewLast()

                    // 현재 보여지는 포지션을 증가시킨다.
                    mCardViewShowPosition++
                    Log.d(TAG, "mCardViewShowPosition++ : $mCardViewShowPosition")

                    // 차일드 갯수만큼 반복한다.
                    for (nCount in 0 until childCount) {

                        // 뷰를 가져온다.
                        val view = getChildAt(nCount)

                        // 스케일을 계산하기 위한 index 값을 구한다.
                        val index = childCount - nCount

                        // 축소시킬 스케일을 계산한다.
                        val scale = (MAX_COUNT - index) / MAX_COUNT.toFloat() * 0.2f + 0.87f

                        // 제일 앞에 있는 뷰르 위로 이동시킨다.
                        if (nCount == childCount - 1) {

                            // 뷰를 맨앞으로 이동시킨다.
                            bringToTop(view)

                        } else {

                            // 마진계산
                            val margin = (index - 1) * VIEW_SPACE

                            if (nCount == 0 && childCount > 2) {

                                // 나머지 뷰들을 한단계 위로 이동시킨다.
                                view.animate()
                                    .translationY(margin.toFloat())
                                    .setInterpolator(AccelerateInterpolator())
                                    .setListener(null)
                                    .scaleX(scale)
                                    .scaleY(scale)
                                    .alpha(0.5f).duration = ANIMATION_DURATION.toLong()
                            } else {

                                // 나머지 뷰들을 한단계 위로 이동시킨다.
                                view.animate()
                                    .translationY(margin.toFloat())
                                    .setInterpolator(AccelerateInterpolator())
                                    .setListener(null)
                                    .scaleX(scale)
                                    .scaleY(scale)
                                    .alpha(1f).duration = ANIMATION_DURATION.toLong()
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
     * Add a new view to the 0th position.
     */
    private fun addNewViewLast() {

        val lastPosition = getShowPosition(mCardViewShowPosition)
        Log.d(TAG, "lastPosition : $lastPosition")
        Log.d(TAG, "mCardViewShowPosition : $mCardViewShowPosition")

        val convertView = mViewHolder.get(0)
        val view = mCardViewAdapter!!.getView(lastPosition, convertView, this)

        // 뷰의 스케일을 조정한다.
        convertView.scaleX = 0.87f
        convertView.scaleY = 0.87f
        convertView.translationY = (VIEW_SPACE * (MAX_COUNT - 1)).toFloat()

        // 뷰를 추가한다.
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
     * 올라간 카드를 다시 내려서 가져온다.
     *
     * @author kwonho87
     * @date 2015. 5. 20. 오후 5:52:04
     */
    private fun goDown() {
        Log.d(TAG, "goDown")

        // 터치가 false이면 그냥 리턴한다.
        if (!mIsDispatchTouch) {
            return
        }

        // 터치 false.
        mIsDispatchTouch = false

        // 이전 뷰를 새로 생성하여 내려오면서 보여준다.
        brindToDown()

        // 차일드 뷰의 갯수가 최대 보여질 뷰의 갯수와 같은 경우에만 제일 마지막 뷰는 제거한다.
        if (childCount > MAX_COUNT) {
            val bottomView = getChildAt(0)
            removeView(bottomView)
        } else if (mIntMax < MAX_COUNT) {
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
            val scaleX = (MAX_COUNT - index) / MAX_COUNT.toFloat() * 0.2f + 0.87f

            // 이동시킬 마진을 계산한다.
            val topMargin = (index - 1) * VIEW_SPACE
            //            Log.d(TAG, "topMargin : " + topMargin);

            // 뷰를 이동시킨다.
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

                        // 터치 true
                        mIsDispatchTouch = true
                    }
                })

            // 새로 추가할 뷰의 퀵버튼을 초기화 한다.
            (view as ItemView).initFlipAnimation(mCardViewShowPosition)

            // holder에 추가한다.
            mViewHolder.put(childCount - nCount - 1, view)

            // 만약 차일드뷰가 3개인 경우 마지막 뷰는 투명도를 적용한다.
            if (nCount == 0 && childCount == MAX_COUNT) {
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
        val convertView = mViewHolder.get(MAX_COUNT)

        // 새로 추가할 뷰
        val newView: View

        // 만약 차일드갯수가 3개인 경우
        if (mIntMax == MAX_COUNT) {

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
            .scaleY(1f).duration = ANIMATION_DURATION.toLong()
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