<<<<<<< HEAD
package com.kyoapps.maniac.functions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import androidx.annotation.ColorInt
import android.util.TypedValue

object FuncUi {

    fun makeSelector(colorSelected: Int, colorPressed: Int, colorNotSelected: Int): StateListDrawable {
        val res = StateListDrawable()
        res.addState(intArrayOf(android.R.attr.state_selected), ColorDrawable(colorSelected))
        res.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(colorPressed))
        res.addState(intArrayOf(-android.R.attr.state_selected), ColorDrawable(colorNotSelected))
        return res
    }

    fun makeColorStateList(colorSelected: Int, colorPressed: Int, colorNotSelected: Int): ColorStateList {
        val states = arrayOf(intArrayOf(android.R.attr.state_selected), // selected
                intArrayOf(android.R.attr.state_pressed), // pressed
                intArrayOf(-android.R.attr.state_selected) // not selected
        )
        val colors = intArrayOf(colorSelected, colorPressed, colorNotSelected)
        return ColorStateList(states, colors)
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
=======
package com.kyoapps.maniac.functions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.support.annotation.ColorInt
import android.util.TypedValue

object FuncUi {

    fun makeSelector(colorSelected: Int, colorPressed: Int, colorNotSelected: Int): StateListDrawable {
        val res = StateListDrawable()
        res.addState(intArrayOf(android.R.attr.state_selected), ColorDrawable(colorSelected))
        res.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(colorPressed))
        res.addState(intArrayOf(-android.R.attr.state_selected), ColorDrawable(colorNotSelected))
        return res
    }

    fun makeColorStateList(colorSelected: Int, colorPressed: Int, colorNotSelected: Int): ColorStateList {
        val states = arrayOf(intArrayOf(android.R.attr.state_selected), // selected
                intArrayOf(android.R.attr.state_pressed), // pressed
                intArrayOf(-android.R.attr.state_selected) // not selected
        )
        val colors = intArrayOf(colorSelected, colorPressed, colorNotSelected)
        return ColorStateList(states, colors)
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
>>>>>>> 8a1e94693e63922ed0fd786654e66d081f7a6a63
}