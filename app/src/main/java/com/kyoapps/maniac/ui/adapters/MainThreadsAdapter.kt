package com.kyoapps.maniac.ui.adapters

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.annotation.ColorInt
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.kyoapps.maniac.R
import com.kyoapps.maniac.dagger.components.DaggerActivityComponent

import com.kyoapps.maniac.functions.FuncFetch
import com.kyoapps.maniac.functions.FuncUi
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.helpers.classes.ThreadDisplaySettingsItem
import com.kyoapps.maniac.room.entities.ThreadEnt
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM

class MainThreadsAdapter(val context: Context?, private val slidingPaneLayout: androidx.slidingpanelayout.widget.SlidingPaneLayout?, private val component: DaggerActivityComponent)
    : ListAdapter<ThreadEnt, androidx.recyclerview.widget.RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var lastSelectedId = -1L

    @ColorInt private val colorSelected = FuncUi.getAttrColorData(context, R.attr.colorPrimary)
    @ColorInt private val colorNotSelectedUnread = context?.resources?.getColor(R.color.grey_trans_2) ?: Color.GRAY
    @ColorInt private val textColorNotSelectedUnread = FuncUi.getAttrColorData(context, R.attr.textColorStrong)
    @ColorInt private val textColorNotSelectedRead = FuncUi.getAttrColorData(context, R.attr.textColorDim)
    private val textBackgroundUnread = FuncUi.makeColorStateList(Color.WHITE, Color.WHITE, textColorNotSelectedUnread)
    private val textBackgroundRead = FuncUi.makeColorStateList(Color.WHITE, Color.WHITE, textColorNotSelectedRead)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.main_thread_row, parent, false)
        view.background = FuncUi.makeSelector(colorSelected, colorSelected, colorNotSelectedUnread)

        return ThreadViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val threadEnt = getItem(position)
        if (threadEnt != null) {
            (holder as ThreadViewHolder).bindTo(threadEnt)
            holder.title.setTextColor(textBackgroundUnread)
            holder.count.setTextColor(textBackgroundUnread)
            holder.view.isSelected = lastSelectedId == getItemId(position)
        }

        (holder as ThreadViewHolder).view.setOnClickListener { _->
            getItem(holder.adapterPosition)?.let {
                Log.i(TAG, "read MainThreadsAdapter brdid ${it.brdid} thrdid ${it.thrdid}")

                if (it.thrdid.toLong() != lastSelectedId) {
                    setLastSelected(holder, it.thrdid.toLong())
                    FuncFetch.fetchReplies(component.mainVM, component.mainDS, LoadRequestItem(it.brdid, it.thrdid, null))
                    component.mainVM.setRepliesRequestItem(LoadRequestItem(it.brdid, it.thrdid, null))
                    //Toast.makeText(context, "click; request: ${LoadRequestItem(it.brdid, it.thrdid, null)}", Toast.LENGTH_SHORT).show()
                }
                Handler().postDelayed({slidingPaneLayout?.closePane()}, 480)
            }
        }
    }


    class ThreadViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
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

    fun getPosFromId(id: Long): Int {
        for (i in 0..(itemCount-1)) {
            if (getItemId(i) == id) return i
        }
        return -1
    }

    fun setLastSelected(holder: ThreadViewHolder?, id: Long) {
        if (lastSelectedId != id) {
            holder?.view?.isSelected = true
            // remove highlight from last selected row
            if (lastSelectedId != -1L) notifyItemChanged(getPosFromId(lastSelectedId))
            lastSelectedId = id
            // if (holder == null) refresh row to show it selected
            if (holder == null) notifyItemChanged(getPosFromId(lastSelectedId))
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

            override fun getChangePayload(oldItem: ThreadEnt, newItem: ThreadEnt): Any? {
                val diffBundle = Bundle()
                if (newItem.subject != oldItem.subject)  diffBundle.putString("KEY_SUBJECT", newItem.subject)
                if (newItem.totalReplies != oldItem.totalReplies) diffBundle.putInt("KEY_TOTAL_REPLIES", newItem.totalReplies)
                if (newItem.oldReplies != oldItem.oldReplies) diffBundle.putInt("KEY_OLD_REPLIES", newItem.totalReplies)
                if (diffBundle.size() == 0) return null
                return diffBundle
            }
        }
    }

}

