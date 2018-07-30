package com.kyoapps.maniac.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.kyoapps.maniac.R
import com.kyoapps.maniac.dagger.components.ActivityComponent
import com.kyoapps.maniac.dagger.components.DaggerActivityComponent
import com.kyoapps.maniac.dagger.modules.ContextModule
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.ui.adapters.MainRepliesPagedAdapter
import android.support.annotation.ColorInt
import android.util.TypedValue
import com.kyoapps.maniac.functions.FuncParse


class MainRepliesFrag : Fragment() {

    private lateinit var component: ActivityComponent
    private var lastRequest: LoadRequestItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        component = DaggerActivityComponent.builder()
                .contextModule(ContextModule(activity as Context))
                .build()

        return inflater.inflate(R.layout.main_replies_frag, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // get reply list and fetch first reply
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_replies)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        val adapter = MainRepliesPagedAdapter(component.mainVM, component.defaultSettings)
        recyclerView.adapter = adapter


        component.mainVM.repliesLiveDataPaged()?.observe(this, Observer {
                it?.let {
                if (it.isNotEmpty()) it[0]?.let {
                    if (component.mainVM.getLatestRequestItem().value?.thrdid != it.thrdid) {
                        component.mainVM.setMessageRequestItem(LoadRequestItem(it.brdid, it.thrdid, it.msgid))
                    }
                }
                adapter.submitList(it)
            }
        })

        component.mainVM.getLatestRequestItem().observe(this, Observer {
            it?.let {
                lastRequest = it
                if (it.msgid != null) adapter.setlastSelected(it.msgid.toLong())
            }
        })


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

            component.mainVM.getMessageLiveDataRx()?.observe(this, Observer {
                it?.data?.let{
                    val formattedText = FuncParse.formatHtmlLegacy(it, adapter.getSubjectFromId(lastRequest?.msgid?.toLong()), textColor, quoteColor, linkColor, " ")
                    ww.loadData(formattedText, "text/html; charset=utf-8", "UTF-8")
                }
            })
        }
    }


    companion object {
        //private const val TAG = "MainRepliesFrag"
    }
}
