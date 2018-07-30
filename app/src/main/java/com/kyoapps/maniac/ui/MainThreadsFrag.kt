package com.kyoapps.maniac.ui

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.widget.SlidingPaneLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.Spinner
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
import android.widget.ArrayAdapter
import com.kyoapps.maniac.helpers.classes.pojo.Board
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types


class MainThreadsFrag : Fragment() {

    private lateinit var component: ActivityComponent
    private lateinit var compositeDisposable: CompositeDisposable
    private var lastRequest = LoadRequestItem(1, 168250, 4224606)
    private var requestThreadsLayoutRefresh = true
    private var openPane = false

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

        // request (board id, thread id, message id) from last session
        val previousRequest = Moshi.Builder().build().adapter(LoadRequestItem::class.java).fromJson(
                component.defaultSettings.getString(C_SETTINGS.PREVIOUS_REQUEST,
                        Moshi.Builder().build().adapter(LoadRequestItem::class.java).toJson(lastRequest)))

        Log.i(TAG, "savedInstanceState == null: ${savedInstanceState == null}")
        if (savedInstanceState == null) {
            // load deeplink if present. Else load last opened
            if (arguments?.get("mode") != null) {

                loadDeeplink(arguments?.get("mode") as String)
            } else {
                previousRequest?.let {
                    initBoardSpinner(it)
                    fetchOnline(it)
                    loadFromDb(it)
                }
            }
        } else { initBoardSpinner(previousRequest) }


        val recyclerView: RecyclerView = view.findViewById(R.id.rv_threads)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        val slidingPaneLayout: SlidingPaneLayout? = activity?.findViewById(R.id.pane_main)

        val adapter: MainThreadsAdapter? = MainThreadsAdapter(slidingPaneLayout, component.mainVM, component.mainDS,
                ThreadDisplaySettingsItem(2, false, R.layout.main_thread_row))

        recyclerView.adapter = adapter

        component.mainVM.threadsLiveDataRx()?.observe(this, Observer {
            // delay load for smoother animation. load immediately if slidingPane already open
                it?.data?.let {
                    if (it.isNotEmpty() && requestThreadsLayoutRefresh) {
                        requestThreadsLayoutRefresh = false
                        if (slidingPaneLayout?.isOpen == true) adapter?.submitList(it)
                        else Handler().postDelayed({ adapter?.submitList(it) }, 400)
                    }
                }
        })

        component.mainVM.getLatestRequestItem().observe(this, Observer {
            it?.let {
                Log.i(TAG, "latest msgId: ${it.msgid}")
                it.thrdid?.let { adapter?.setLastSelected(it.toLong()) }
                lastRequest = it
               }
        })

    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.day_night -> {
                component.defaultSettings.edit().putBoolean(C_SETTINGS.NIGHT_MODE, !component.defaultSettings.getBoolean(C_SETTINGS.NIGHT_MODE, true)).apply()
                activity?.recreate()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDeeplink(deeplink: String) {
        Log.i(TAG, "deeplink $deeplink")
        compositeDisposable.add(component.mainDS.parseManiacUrl(deeplink)
                .filter { it.brdid.toInt() != -1 && it.thrdid != -1 }
                .subOnNewObsOnMain()
                .subscribe({
                    initBoardSpinner(it)
                    loadFromDb(it)
                    fetchOnline(it)
                }, { t -> Log.e(TAG, t.localizedMessage) }))
    }


    private fun loadFromDb(requestItem: LoadRequestItem) {
        Log.i(TAG, "loadFromDb brd ${requestItem.brdid} thrd ${requestItem.thrdid} msg ${requestItem.msgid}")
        component.mainVM.setThreadsRequestItem(requestItem)
        component.mainVM.setRepliesRequestItem(requestItem)
    }

    private fun fetchOnline(requestItem: LoadRequestItem) {
        Log.i(TAG, "fetchOnline brd ${requestItem.brdid} thrd ${requestItem.thrdid} msg ${requestItem.msgid}")
        updateBoards(requestItem)
        FuncFetch.fetchThreads(component.mainDS, requestItem)
        FuncFetch.fetchReplies(component.mainDS, requestItem)
        if (requestItem.msgid != null) component.mainVM.setMessageRequestItem(requestItem)
    }

    private fun initBoardSpinner(requestItem: LoadRequestItem?) {
        val spinner: Spinner? = activity?.findViewById(R.id.sp_boards)
        val moshiAdapter: JsonAdapter<List<Board>> = Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, Board::class.java))
        val oldList = moshiAdapter.fromJson(component.defaultSettings.getString(C_SETTINGS.BOARDS, moshiAdapter.toJson(ArrayList(0))))
        setSpinner(spinner, oldList!!, requestItem)
    }

    private fun updateBoards(loadRequestItem: LoadRequestItem) {
        component.mainVM.getBoardsLiveDataRx()?.observe(this, Observer {
            it?.data?.let {
                val spinner: Spinner? = activity?.findViewById(R.id.sp_boards)
                setSpinner(spinner, it, loadRequestItem)
                val moshiAdapter: JsonAdapter<List<Board>> = Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, Board::class.java))
                component.defaultSettings.edit().putString(C_SETTINGS.BOARDS, moshiAdapter.toJson(it)).apply()
            }
        })
    }

    private fun setSpinner(spinner: Spinner?, boardList: List<Board>, requestItem: LoadRequestItem?) {
        spinner?.adapter = ArrayAdapter(activity, R.layout.main_spinner, boardList.map { it.label })
        if (boardList.map { it.brdid }.contains(requestItem?.brdid)) { spinner?.setSelection(boardList.map { it.brdid }.indexOf(requestItem?.brdid)) }

        // only open pane if user selects board manually
        spinner?.setOnTouchListener { _, _ ->
            openPane = true
            return@setOnTouchListener false
        }

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d(TAG,  "spinner")
                requestThreadsLayoutRefresh = true
                if (openPane) {
                    activity?.findViewById<SlidingPaneLayout>(R.id.pane_main)?.openPane()
                    openPane = false
                }
                val loadThreadsRequest = LoadRequestItem(boardList[position].brdid, null, null)
                component.mainVM.setThreadsRequestItem(loadThreadsRequest)
                FuncFetch.fetchThreads(component.mainDS, loadThreadsRequest)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }



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
        private const val TAG = "MainThreadsFrag"
    }
}
