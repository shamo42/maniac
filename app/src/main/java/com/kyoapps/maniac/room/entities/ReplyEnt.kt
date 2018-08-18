package com.kyoapps.maniac.room.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "reply")
data class ReplyEnt(

        val brdid: Short,
        val thrdid: Int,
        @PrimaryKey
        val msgid: Int,
        val subject: String,
        val user: String,
        val replyTime: String,
        var read: Boolean,
        val fetchTime: Long
)
