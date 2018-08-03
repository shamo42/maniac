package com.kyoapps.maniac.functions

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.support.annotation.ColorInt
import android.util.TypedValue

object FuncUi {

    fun makeSelector(colorSelected: Int, colorPressed: Int): StateListDrawable {
        val res = StateListDrawable()
        res.addState(intArrayOf(android.R.attr.state_selected), ColorDrawable(colorSelected))
        res.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(colorPressed))
        res.addState(intArrayOf(-android.R.attr.state_selected), ColorDrawable(Color.TRANSPARENT))
        return res
    }

    @ColorInt
    fun getAttrColorData(context: Context?, colorId: Int): Int {
        if (context != null) {
            val typedValue = TypedValue()
            val theme = context.theme
            theme?.resolveAttribute(colorId, typedValue, true)
            return typedValue.data
        }
        return Color.GRAY
    }

    private const val TAG = "FuncUi"
}