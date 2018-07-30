package com.kyoapps.maniac.ui.adapters

import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SlidingPaneLayout
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.kyoapps.maniac.R
import com.kyoapps.maniac.functions.FuncFetch
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.helpers.classes.ThreadDisplaySettingsItem
import com.kyoapps.maniac.room.entities.ThreadEnt
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM

class MainThreadsAdapter(private val slidingPaneLayout: SlidingPaneLayout?, private val mainVM: MainVM,
                         private val mainDS: MainDS, private val settings: ThreadDisplaySettingsItem)
    : ListAdapter<ThreadEnt, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var lastSelectedId = -1L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.main_thread_row, parent, false)
        return ThreadViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val threadEnt = getItem(position)
        if (threadEnt != null) {
            (holder as ThreadViewHolder).bindTo(threadEnt)
            holder.view.isSelected = lastSelectedId == getItemId(position)
        }

        (holder as ThreadViewHolder).view.setOnClickListener {
            getItem(holder.adapterPosition)?.let {
                Log.d(TAG, "clicked MainThreadsAdapter brdid ${it.brdid} thrdid ${it.thrdid}")

                if (it.thrdid.toLong() != lastSelectedId) {
                    FuncFetch.fetchReplies(mainDS, LoadRequestItem(it.brdid, it.thrdid, null))
                    mainVM.setRepliesRequestItem(LoadRequestItem(it.brdid, it.thrdid, null))
                }
                Handler().postDelayed({slidingPaneLayout?.closePane()}, 480)
            }
        }
    }


    class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.tv_thread_row_title)
        var count: TextView = itemView.findViewById(R.id.tv_thread_row_replycount)
        var view = itemView

        fun bindTo(replyEnt: ThreadEnt) {
            title.text = replyEnt.subject
            count.text = replyEnt.totalReplies.toString()

        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).thrdid.toLong()
    }

    private fun getPosFromId(id: Long): Int {
        for (i in 0..(itemCount-1)) {
            if (getItemId(i) == id) return i
        }
        return -1
    }

    fun setLastSelected(id: Long) {
        if (lastSelectedId != id) {
            notifyItemChanged(getPosFromId(lastSelectedId))
            lastSelectedId = id
            notifyItemChanged(getPosFromId(id))
        }
    }

    companion object {
        private const val TAG = "MainThreadsAdapter"
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ThreadEnt>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(oldThread: ThreadEnt, newThread: ThreadEnt): Boolean =
                    oldThread.thrdid == newThread.thrdid

            override fun areContentsTheSame(oldThread: ThreadEnt, newThread: ThreadEnt): Boolean =
                    oldThread.subject == newThread.subject
                            && oldThread.oldReplies == newThread.oldReplies
                            && oldThread.totalReplies == newThread.totalReplies

            override fun getChangePayload(oldItem: ThreadEnt?, newItem: ThreadEnt?): Any? {
                val diffBundle = Bundle()
                if (newItem?.subject != oldItem?.subject)  diffBundle.putString("KEY_SUBJECT", newItem?.subject)
                if (newItem?.totalReplies != oldItem?.totalReplies) diffBundle.putInt("KEY_TOTAL_REPLIES", newItem?.totalReplies?: 0)
                if (newItem?.oldReplies != oldItem?.oldReplies) diffBundle.putInt("KEY_OLD_REPLIES", newItem?.totalReplies?: 0)
                if (diffBundle.size() == 0) return null
                return diffBundle
            }
        }
    }

}

