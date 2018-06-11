package com.kyoapps.maniac.room.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "thread")
data class ThreadEnt(
        val brdid: Short,
        @PrimaryKey
        val thrdid: Int,
        val subject: String,
        val totalReplies: Int,
        var oldReplies: Int,
        var hide: Boolean,
        val fetchTime: Long
        )

