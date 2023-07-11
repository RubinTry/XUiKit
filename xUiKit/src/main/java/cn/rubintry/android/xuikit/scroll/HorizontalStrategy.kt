package cn.rubintry.android.xuikit.scroll

import android.graphics.RectF
import android.view.View
import android.widget.ListView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import cn.rubintry.android.xuikit.core.IStrategy

class HorizontalStrategy private constructor(): IStrategy{

    companion object{
        @JvmStatic
        fun create(): HorizontalStrategy {
            return HorizontalStrategy()
        }
    }

    override fun checkViewCanScroll(view: View?) {
        assert(view is ScrollView || view is NestedScrollView || view is RecyclerView || view is ListView){"必须传入一个带有滚动特性的View"}
    }

    override fun create(
        view: View?,
        slideSize: Int,
        width: Int,
        height: Int
    ): RectF {
        checkViewCanScroll(view)
        TODO("暂未实现创建滑块的具体逻辑")
    }

    override fun calculateViewTotalLength(view: View?): Int {
        checkViewCanScroll(view)
        return when(view){
            is RecyclerView -> {
                view.computeHorizontalScrollRange()
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
                view.computeHorizontalScrollOffset()
            }
            else -> {
                0
            }
        }
    }

    override fun calculateViewExtent(view: View?): Int {
        TODO("Not yet implemented")
    }


}