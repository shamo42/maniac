package com.kyoapps.maniac.ui

import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.kyoapps.maniac.R
import com.kyoapps.maniac.dagger.components.ActivityComponent
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.ui.adapters.MainRepliesPagedAdapter
import androidx.annotation.ColorInt
import android.util.TypedValue
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kyoapps.maniac.dagger.components.DaggerActivityComponent
import com.kyoapps.maniac.functions.FuncParse


class MainRepliesFrag : androidx.fragment.app.Fragment() {

    private lateinit var component: ActivityComponent
    private var lastRequest: LoadRequestItem? = null
    private var newThread = true

    private var recyclerView: RecyclerView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        component = DaggerActivityComponent.builder()
                .applicationContext(activity as Context)
                .build()


        return inflater.inflate(R.layout.main_replies_frag, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get reply list and fetch first reply
        recyclerView = view.findViewById(R.id.rv_replies)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.setHasFixedSize(true)
        //recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL))

        val adapter = MainRepliesPagedAdapter(context, component as DaggerActivityComponent)
        recyclerView?.adapter = adapter

        component.mainVM.repliesLiveDataPaged()?.observe(viewLifecycleOwner, Observer { pagedList ->
            activity?.findViewById<SwipeRefreshLayout>(R.id.srl_replies)?.isRefreshing = false
            if (pagedList != null && pagedList.isNotEmpty()) {
                adapter.submitList(pagedList)

                // if opened new thread load first reply
                pagedList[0]?.let {
                    if (component.mainVM.getLatestRequestItem().value?.thrdid != it.thrdid) {
                        component.mainVM.setMessageRequestItem(LoadRequestItem(it.brdid, it.thrdid, it.msgid))
                    }
                }

                if (newThread) {

                    lastRequest?.msgid?.let { msgid ->
                        val pos = adapter.getPosFromId(msgid.toLong())
                        if (pos > 0) (recyclerView?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(pos, 200)
                    }
                }
            }
        })

        component.mainVM.getLatestRequestItem().observe(viewLifecycleOwner, Observer { requestItem ->
            requestItem?.let {
                newThread = it.thrdid != lastRequest?.thrdid
                if (newThread) adapter.setMarkRead()
                lastRequest = it
                if (it.msgid != null)  adapter.setLastSelected(null, it.msgid.toLong())
            }
        })

        activity?.findViewById<SwipeRefreshLayout>(R.id.srl_replies)?.setOnRefreshListener {
            if (lastRequest != null) component.mainVM.setRepliesRequestItem(lastRequest!!)
        }


        val webView = activity?.findViewById<WebView>(R.id.ww_main)
        webView?.let { ww ->

            val typedValue = TypedValue()
            val theme = context?.theme
            theme?.resolveAttribute(R.attr.textColorStrong, typedValue, true)
            @ColorInt val textColor = typedValue.data
            theme?.resolveAttribute(R.attr.textColorDim, typedValue, true)
            @ColorInt val quoteColor = typedValue.data
            theme?.resolveAttribute(R.attr.textColorLink, typedValue, true)
            @ColorInt val linkColor = typedValue.data
            theme?.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
            @ColorInt val backGroundColor = typedValue.data

            ww.settings.javaScriptEnabled = true
            ww.settings.defaultFontSize = 14
            ww.setBackgroundColor(backGroundColor)

            component.mainVM.getMessageLiveDataRx()?.observe(viewLifecycleOwner, Observer { resource ->
                resource?.extractData?.let{
                    val formattedText = FuncParse.formatHtmlLegacy(it, adapter.getSubjectFromId(lastRequest?.msgid?.toLong()), textColor, quoteColor, linkColor, " ")
                    ww.loadData(formattedText, "text/html; charset=utf-8", "UTF-8")
                    component.mainVM.setIsLoadingMsg(false)
                }
            })
        }
    }


    override fun onDetach() {
        (recyclerView?.adapter as MainRepliesPagedAdapter).setMarkRead()
        super.onDetach()
    }

    companion object {
        private const val TAG = "MainRepliesFrag"
    }
}
