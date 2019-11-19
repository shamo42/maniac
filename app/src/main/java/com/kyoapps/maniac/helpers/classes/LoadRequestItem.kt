package com.kyoapps.maniac.helpers.classes

//@JsonClass(generateAdapter = true)
data class LoadRequestItem(
    val brdid: Int,
    val thrdid: Int?,
    val msgid: Int?
)
