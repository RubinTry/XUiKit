package cn.rubintry.android.xuikit.scroll

import cn.rubintry.android.xuikit.core.HORIZONTAL
import cn.rubintry.android.xuikit.core.IStrategy

class ScrollStrategyFactory {

    companion object{
        @JvmStatic
        fun create(orientation: Int) : IStrategy{
            return if(orientation == HORIZONTAL){
                HorizontalStrategy.create()
            }else{
                VerticalStrategy.create()
            }
        }
    }
}