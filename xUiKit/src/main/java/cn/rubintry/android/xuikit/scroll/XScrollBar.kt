package cn.rubintry.android.xuikit.scroll


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ListView
import android.widget.ScrollView
import androidx.core.graphics.toRect
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import cn.rubintry.android.xuikit.R
import cn.rubintry.android.xuikit.core.HORIZONTAL
import cn.rubintry.android.xuikit.core.IStrategy
import cn.rubintry.android.xuikit.core.VERTICAL
import kotlin.math.max
import kotlin.math.roundToInt


/**
 * 滚动条
 * @author rubintry
 * @since 20230711
 * 目前已经支持[RecyclerView]的联动
 */
class XScrollBar : View {
    private var drawSlider = false

    /**
     * 滑块图片
     */
    private var slideImage : Drawable ?= null

    /**
     * 滑倒底部时总长度可能会变，maxLength的作用就是实时记录总长度，以至于后面用maxLength来确定滑块的位置
     */
    private var maxLength: Int = 0

    private var currentOffset : Int= 0

    /**
     * 需要联动的可滚动视图，如：NestedScrollView、ScrollView、RecyclerView、ListView
     */
    private var scrollableView: View ?= null

    /**
     * 朝向
     */
    private var orientation: Int = VERTICAL

    /**
     * 滚动偏移量相关辅助类
     */
    private var scrollStrategy: IStrategy ?= null

    /**
     * 滑块宽度
     */
    private var slideViewWidth = 0

    /**
     * 滑块高度
     */
    private var slideViewHeight = 0

    /**
     * 滑块圆角半径
     */
    private var slideRadius = 0

    private var globalPaint = Paint()

    /**
     * 手势检测
     */
    private val gestureDetector by lazy { GestureDetector(context , gestureListener) }

    /**
     * 是否触碰滑块
     */
    private var touched : Boolean? = null

    /**
     * 上一次移动滑块的比率
     */
    private var lastMoveOffsetRadio = 0f

    /**
     * scrollView比率（可见范围 / 整个View范围）
     */
    private var scrollViewRadio : Float ?= null


    /**
     * 滑块区域，用于处理手势，需要在onDraw时就记录好滑块区域信息
     */
    private val sliderRegion = Region()

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener(){
        override fun onDown(e: MotionEvent?): Boolean {
            if (e?.action == MotionEvent.ACTION_DOWN) {
                scrollViewRadio = null
                lastMoveOffsetRadio = 0f
                touched = null
            }
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (touched == null) {
                touched = sliderRegion.contains(e1.x.toInt(), e1.y.toInt())
            }
            if(touched == true){
                scrollableView?.let {
                    val barLength: Int
                    val offsetRatio:Float
                    if (orientation == VERTICAL) {
                        barLength = height - sliderRegion.bounds.height()
                        if (barLength == 0){
                            return true
                        }
                        if (scrollViewRadio == null) {
                            scrollViewRadio = sliderRegion.bounds.top.toFloat() / barLength
                        }
                        offsetRatio = (e2.y - e1.y) / barLength
                    } else {
                        barLength = width - sliderRegion.bounds.width()
                        if (barLength == 0){
                            return true
                        }
                        if (scrollViewRadio == null) {
                            scrollViewRadio = sliderRegion.bounds.left.toFloat() / barLength
                        }
                        offsetRatio = (e2.x - e1.x) / barLength
                    }

                    scrollRecyclerViewByDistance(offsetRatio)
                }
                return true
            }
            return false
        }
    }

    init {
        globalPaint = Paint()
        globalPaint.style = Paint.Style.FILL
    }

    constructor(context: Context?) : super(context){
        initAttrs(context , null)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        initAttrs(context , attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        initAttrs(context , attrs)
    }

    @SuppressLint("Recycle")
    private fun initAttrs(context: Context?, attrs: AttributeSet?){
        val typedArray = context?.obtainStyledAttributes(attrs , R.styleable.XScrollBar)
        orientation = typedArray?.getInt(R.styleable.XScrollBar_android_orientation , VERTICAL) ?: VERTICAL
        scrollStrategy = ScrollStrategyFactory.create(orientation)
        slideImage = typedArray?.getDrawable(R.styleable.XScrollBar_android_src)
        slideViewWidth = typedArray?.getDimensionPixelOffset(R.styleable.XScrollBar_slideViewWidth , 0) ?: 0
        slideViewHeight = typedArray?.getDimensionPixelOffset(R.styleable.XScrollBar_slideViewHeight , 0) ?: 0
        slideRadius = typedArray?.getDimensionPixelOffset(R.styleable.XScrollBar_slideRadius , 0) ?: 0
        typedArray?.recycle()
    }

    /**
     *  操作滑动
     */
    private fun scrollRecyclerViewByDistance(offsetRatio: Float) {
        when(scrollableView){
            is RecyclerView -> {
                scrollableView ?: return
                scrollStrategy ?: return
                (scrollableView as RecyclerView).adapter ?:return
                (scrollableView as RecyclerView).layoutManager ?:return
                if (orientation == VERTICAL){
                    (scrollableView as RecyclerView).scrollBy(0, ((scrollStrategy!!.calculateViewTotalLength(scrollableView) - scrollableView!!.height) * (offsetRatio - lastMoveOffsetRadio)).roundToInt())
                }else{
                    (scrollableView as RecyclerView).scrollBy(((scrollStrategy!!.calculateViewTotalLength(scrollableView) - scrollableView!!.width) * (offsetRatio - lastMoveOffsetRadio)).roundToInt(),0)
                }
                lastMoveOffsetRadio = offsetRatio
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if(scrollableView != null){
            gestureDetector.onTouchEvent(event)
        }else{
            super.onTouchEvent(event)
        }

    }

    fun bindScrollView(scrollableView : View){
        check(this.scrollableView == null){"已经绑定过scrollableView了"}
        scrollStrategy?.checkViewCanScroll(scrollableView)
        this.scrollableView = scrollableView
        addListener()
    }

    /**
     * 添加监听器
     */
    private fun addListener() {
        when(scrollableView){
            is RecyclerView -> {
                //添加滚动监听
                (scrollableView as RecyclerView).addOnScrollListener(object : RecyclerView.OnScrollListener(){
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        println("view---的总高度为：${scrollStrategy?.calculateViewTotalLength(scrollableView)}")
                        println("view---滑过的高度为：${scrollStrategy?.calculateViewScrolledOffset(scrollableView)}")
                        calculateLengthAndOffset()
                        postInvalidate()
                    }
                })
                //添加数据变化监听
                (scrollableView as RecyclerView).adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
                    override fun onChanged() {
                        scrollableView?.postDelayed({
                            maxLength = 0
                            calculateLengthAndOffset()
                            if((scrollableView as RecyclerView).adapter?.itemCount != 0) {
                                drawSlider = true
                            }
                            postInvalidate()
                        } , 300)
                    }
                })
            }
            is ListView -> {

            }
            is NestedScrollView -> {

            }
            is ScrollView -> {

            }
        }
    }

    /**
     * 计算总长度以及偏移量（滑倒底部时总长度可能会变，maxLength的作用就是实时记录总长度，以至于后面用maxLength来确定滑块的位置）
     */
    private fun calculateLengthAndOffset() {
        maxLength = scrollStrategy?.calculateViewTotalLength(scrollableView) ?: 0
        currentOffset = scrollStrategy?.calculateViewScrolledOffset(scrollableView) ?: 0
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        assert(scrollStrategy != null){"未初始化"}
        if(!drawSlider){
            return
        }
        val slideSize = max(slideViewWidth , slideViewHeight)
        val slideRect = scrollStrategy!!.create(scrollableView , slideSize , width , height)

        //绘制滑块白底
        globalPaint.color = Color.WHITE
        canvas?.drawRoundRect(slideRect , slideRadius.toFloat() , slideRadius.toFloat() , globalPaint)

        //绘制滑块图片
        //以下代码已经排除slideImage为空的情况
        if(slideImage is BitmapDrawable && slideRect != null){
            val bitmap = (slideImage as BitmapDrawable).bitmap
            val matrix = Matrix()
            val bitmapSize = max(bitmap.width , bitmap.height)
            if(orientation == HORIZONTAL){
                matrix.setTranslate(slideRect.left , 0f)
            }else{
                matrix.setTranslate(0f , slideRect.top)
            }
            matrix.preScale(slideRect.width() / bitmapSize , slideRect.height() / bitmapSize)
            canvas?.drawBitmap(bitmap , matrix , null)
        }

        sliderRegion.set(slideRect.toRect())
    }
}