package com.kyoapps.maniac.ui.adapters

import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.arch.paging.PagedListAdapter
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.util.Log
import android.view.View
import com.kyoapps.maniac.R
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.room.entities.ReplyEnt
import com.kyoapps.maniac.viewmodel.MainVM


class MainRepliesPagedAdapter(private val mainVM: MainVM, private val settings: SharedPreferences): PagedListAdapter<ReplyEnt, MainRepliesPagedAdapter.ReplyViewHolder>(DIFF_CALLBACK) {

    private var lastSelectedId = -1L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.main_reply_row, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val replyEnt = getItem(position)
        if (replyEnt != null) {
            holder.bindTo(replyEnt)
            holder.view.isSelected = lastSelectedId == getItemId(position)
        }

        holder.view.setOnClickListener {
            getItem(holder.adapterPosition)?.let {
                Log.i(TAG, "MainRepliesPagedAdapter clicked thrdid ${it.thrdid} msgid ${it.msgid}")

                mainVM.setMessageRequestItem(LoadRequestItem(it.brdid, it.thrdid, it.msgid))
            }
        }
    }

    class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.tv_main_reply_title)
        var user: TextView = itemView.findViewById(R.id.tv_main_reply_user)
        var view: View = itemView

        fun bindTo(replyEnt: ReplyEnt) {
            title.text = replyEnt.subject
            user.text = replyEnt.user
        }
    }

    override fun getItemId(position: Int): Long {
        getItem(position)?.msgid?.let { return it.toLong() }
        return -1
    }

    private fun getPosFromId(id: Long): Int {
        for (i in 0..(itemCount-1)) {
            if (getItemId(i) == id) return i
        }
        return -1
    }

    fun getSubjectFromId(id: Long?): String? {
        id?.let {
            return getItem(getPosFromId(it))?.subject
        }
        return null
    }

    fun setlastSelected(id: Long) {
        if (lastSelectedId != id) {
            notifyItemChanged(getPosFromId(lastSelectedId))
            lastSelectedId = id
            notifyItemChanged(getPosFromId(id))
        }
    }


    companion object {
        private const val TAG = "MainRepliesAdapter"
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ReplyEnt>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(oldReply: ReplyEnt, newReply: ReplyEnt): Boolean =
                    oldReply.msgid == newReply.msgid

            override fun areContentsTheSame(oldReply: ReplyEnt, newReply: ReplyEnt): Boolean =
                    oldReply.subject == newReply.subject
                            && oldReply.user == newReply.user
                            && oldReply.replyTime == newReply.replyTime
                            && oldReply.clicked == newReply.clicked

            override fun getChangePayload(oldItem: ReplyEnt?, newItem: ReplyEnt?): Any? {
                val diffBundle = Bundle()
                if (newItem?.subject != oldItem?.subject)  diffBundle.putString("KEY_SUBJECT", newItem?.subject)
                if (newItem?.user != oldItem?.user) diffBundle.putString("KEY_USER", newItem?.user)
                if (newItem?.replyTime != oldItem?.replyTime) diffBundle.putString("KEY_TIMESTAMP", newItem?.replyTime)
                if (newItem?.clicked != oldItem?.clicked) diffBundle.putBoolean("KEY_CLICKED", newItem?.clicked?: false)
                if (diffBundle.size() == 0) return null
                return diffBundle
            }
        }
    }
}