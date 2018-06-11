package com.kyoapps.maniac.ui

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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


class MainRepliesFrag : Fragment() {

    private lateinit var component: ActivityComponent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        component = DaggerActivityComponent.builder()
                .contextModule(ContextModule(activity as Context))
                .build()

        return inflater.inflate(R.layout.main_replies_frag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_replies)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()

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

        val webView = activity?.findViewById<WebView>(R.id.ww_main)
        webView?.let { ww ->
            //ww.settings.javaScriptEnabled = true

            component.mainVM.getMessageLiveDataRx()?.observe(this, Observer {
                //Log.d(TAG, "first message ${it?.substring(0, 100)}")
                //ww.loadDataWithBaseURL("", it, "text/html", "UTF-8", "")
                ww.loadData(it, "text/html; charset=utf-8", "UTF-8")
            })
        }
    }


    companion object {
        const val TAG = "MainRepliesFrag"
    }
}
