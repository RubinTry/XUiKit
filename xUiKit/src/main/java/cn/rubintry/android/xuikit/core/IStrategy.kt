package cn.rubintry.android.xuikit.core

import android.graphics.RectF
import android.view.View

interface IStrategy {
    /**
     * 检查所关联的view是否像RecyclerView、ScrollView那样带有滚动特性
     */
    fun checkViewCanScroll(view: View?)

    /**
     * 创建滑块
     */
    fun create(view: View?, slideSize: Int, width : Int, height: Int) : RectF


    /**
     * 计算View总长度 ，如果是横向，就是总width，纵向就是总height
     */
    fun calculateViewTotalLength(view: View?) : Int

    /**
     * 计算已经滑过的偏移量
     */
    fun calculateViewScrolledOffset(view: View?): Int


    /**
     * 获取视图可视范围
     */
    fun calculateViewExtent(view: View?) : Int

}