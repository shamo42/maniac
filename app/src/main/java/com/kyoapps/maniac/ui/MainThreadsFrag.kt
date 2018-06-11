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
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import com.kyoapps.maniac.R
import com.kyoapps.maniac.dagger.components.ActivityComponent
import com.kyoapps.maniac.dagger.components.DaggerActivityComponent
import com.kyoapps.maniac.dagger.modules.ContextModule
import com.kyoapps.maniac.functions.FuncFetch
import com.kyoapps.maniac.helpers.C_SETTINGS
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.helpers.classes.ThreadDisplaySettingsItem
import com.kyoapps.maniac.helpers.classes.subOnNewObsOnMain
import com.kyoapps.maniac.ui.adapters.MainThreadsAdapter
import com.squareup.moshi.Moshi
import io.reactivex.disposables.CompositeDisposable

class MainThreadsFrag : Fragment() {

    private lateinit var component: ActivityComponent
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var lastRequest: LoadRequestItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        component = DaggerActivityComponent.builder()
                .contextModule(ContextModule(activity as Context))
                .build()
        compositeDisposable = CompositeDisposable()

        return inflater.inflate(R.layout.main_threads_frag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            if (arguments?.get("mode") != null) { loadDeeplink(arguments?.get("mode") as String) }
            else loadPrevious()
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_threads)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()

        val adapter = MainThreadsAdapter(component.mainVM, component.mainDS,
                ThreadDisplaySettingsItem(2, false, R.layout.main_thread_row, R.color.colorAccent, R.color.colorPrimary))

        recyclerView.adapter = adapter

        component.mainVM.threadsLiveDataRx()?.observe(this, Observer {
            it?.let { adapter.submitList(it) }
        })

        component.mainVM.getLatestRequestItem().observe(this, Observer {
            it?.let {
                Log.d(TAG, "latest msgId: ${it.msgid}")
                lastRequest = it
               }
        })

    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main_menu, menu)
    }


    private fun loadDeeplink(deeplink: String) {
        Log.i(TAG, "deeplink $deeplink")
        compositeDisposable.add(component.mainDS.parseManiacUrl(deeplink)
                .filter { it.brdid.toInt() != -1 && it.thrdid != -1 }
                .subOnNewObsOnMain()
                .subscribe({
                    fetchOnline(it)
                    loadFromDb(it)
                }, { t -> Log.e(TAG, t.localizedMessage) }))
    }

    private fun loadPrevious() {
        Moshi.Builder().build().adapter(LoadRequestItem::class.java).fromJson(
                component.defaultSettings.getString(C_SETTINGS.PREVIOUS_REQUEST,
                        Moshi.Builder().build().adapter(LoadRequestItem::class.java).toJson(LoadRequestItem(1, 168250, 4224606)))
        )?.let {
            Log.d(TAG, "latest msgId restore: ${it.msgid}")
            fetchOnline(it)
            loadFromDb(it)
        }
    }

    private fun loadFromDb(requestItem: LoadRequestItem) {
        Log.i(TAG, "loadFromDb brd ${requestItem.brdid} thrd ${requestItem.thrdid} msg ${requestItem.msgid}")
        component.mainVM.setThreadsRequestItem(requestItem)
        component.mainVM.setRepliesRequestItem(requestItem)
    }

    private fun fetchOnline(requestItem: LoadRequestItem) {
        Log.i(TAG, "fetchOnline brd ${requestItem.brdid} thrd ${requestItem.thrdid} msg ${requestItem.msgid}")
        FuncFetch.fetchThreads(component.mainDS, requestItem)
        FuncFetch.fetchReplies(component.mainDS, requestItem)
        if (requestItem.msgid != null) component.mainVM.setMessageRequestItem(requestItem)
    }


    /*override fun onSaveInstanceState(outState: Bundle) {
        StateSaver.saveInstanceState(this, outState)
        super.onSaveInstanceState(outState)
    }*/

    override fun onDetach() {
        compositeDisposable.clear()
        FuncFetch.clearDisposables()
        super.onDetach()
    }

    override fun onStop() {
        component.defaultSettings.edit().putString(C_SETTINGS.PREVIOUS_REQUEST,
                Moshi.Builder().build().adapter(LoadRequestItem::class.java).toJson(lastRequest)).apply()
        super.onStop()
    }

    companion object {
        const val TAG = "MainThreadsFrag"
    }
}
