package com.wuhenzhizao.adapter.holder

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

/**
 * Created by liufei on 2017/12/4.
 */
class RecyclerViewBindingHolder<out T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root) {
    var layoutId: Int = 0

    inline fun <reified VB> convert(): VB {
        return binding as VB
    }
}