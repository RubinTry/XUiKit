package cn.rubintry.android.xuikit.scroll

import android.graphics.RectF
import android.view.View
import android.widget.ListView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import cn.rubintry.android.xuikit.core.IStrategy

class VerticalStrategy private constructor() : IStrategy{

    companion object{
        @JvmStatic
        fun create(): VerticalStrategy {
            return VerticalStrategy()
        }
    }

    override fun checkViewCanScroll(view: View?) {
        assert(view is ScrollView || view is NestedScrollView || view is RecyclerView || view is ListView){"必须传入一个带有滚动特性的View"}
    }

    override fun create(view: View?, slideSize: Int, width: Int, height: Int): RectF {
        checkViewCanScroll(view)
        return when(view){
            is RecyclerView -> {
                createRecyclerViewSlider(view , slideSize , width , height)
            }

            else -> {
                RectF()
            }
        }
    }



    override fun calculateViewTotalLength(view: View?): Int {
        checkViewCanScroll(view)
        return when(view){
            is RecyclerView -> {
                view.computeVerticalScrollRange()
            }

            is ScrollView -> {
                0
            }

            else -> {
                0
            }
        }
    }

    override fun calculateViewScrolledOffset(view: View?): Int {
        checkViewCanScroll(view)
        return when(view){
            is RecyclerView -> {
                view.computeVerticalScrollOffset()
            }
            else -> {
                0
            }
        }
    }

    override fun calculateViewExtent(view: View?): Int {
        return when(view){
            is RecyclerView -> {
                view.computeVerticalScrollExtent()
            }
            else -> {
                0
            }
        }
    }

    private fun createRecyclerViewSlider(
        view: RecyclerView,
        slideSize: Int,
        width: Int,
        height: Int
    ): RectF {
        val top = if (calculateViewTotalLength(view) == 0) {
            0f
        } else {
            (calculateViewScrolledOffset(view).toFloat() / (calculateViewTotalLength(view) - calculateViewExtent(view))) * (height - slideSize)
        }
        return RectF(0f,top,width.toFloat(),top + slideSize)
    }
}