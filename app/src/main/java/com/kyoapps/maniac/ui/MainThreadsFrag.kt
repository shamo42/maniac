package com.kyoapps.maniac.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kyoapps.maniac.R
import com.kyoapps.maniac.dagger.components.ActivityComponent
import com.kyoapps.maniac.dagger.components.DaggerActivityComponent
import com.kyoapps.maniac.functions.FuncFetch
import com.kyoapps.maniac.helpers.C_SETTINGS
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.helpers.classes.pojo.Board
import com.kyoapps.maniac.ui.adapters.MainThreadsAdapter
import com.kyoapps.zkotlinextensions.extensions.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MainThreadsFrag : Fragment() {

    private val component: ActivityComponent by lazy {
        DaggerActivityComponent.builder()
                .applicationContext(activity as Context)
                .build()
    }
    private lateinit var compositeDisposable: CompositeDisposable
    private var lastRequest: LoadRequestItem? = null
    private var requestThreadsLayoutRefresh = true
    private var loadBoard = false
    private var requestOpenPane = false
    private var scrollToLastPos = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)


        compositeDisposable = CompositeDisposable()

        return inflater.inflate(R.layout.main_threads_frag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // request (board id, thread id, message id) from last session
        //Log.d(TAG, component.defaultSettings.getString(C_SETTINGS.PREVIOUS_REQUEST, LoadRequestItem(1, 168250, 4224606).toJsonString()))
        lastRequest = component.defaultSettings.getString(C_SETTINGS.PREVIOUS_REQUEST, (LoadRequestItem(1, 168250, 4224606).toJsonString()))?.toJsonObject()

        Log.d(TAG, "savedInstanceState == null: ${savedInstanceState == null}")
        if (savedInstanceState == null) {
            // load deeplink if present. Else load last opened
            if (arguments?.get("mode") != null) {
                loadDeepLink(arguments?.get("mode") as String)
            } else {
                lastRequest?.let {
                    initBoardSpinner(it)
                    fetchOnline(it)
                    loadFromDb(it)
                    Handler().postDelayed({updateBoards(it)}, 2000)
                }
            }
        } else {
            initBoardSpinner(lastRequest)
        }


        val recyclerView: RecyclerView = view.findViewById(R.id.rv_threads)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        val slidingPaneLayout: SlidingPaneLayout? = activity?.findViewById(R.id.pane_main)

        @ColorInt val colorPressed = context?.resources?.getColor(R.color.grey_trans_2, activity?.theme)?: Color.GRAY

        val adapter: MainThreadsAdapter? = MainThreadsAdapter(activity, slidingPaneLayout, component as DaggerActivityComponent, compositeDisposable)

        recyclerView.adapter = adapter

        component.mainVM.threadsLiveDataRx()?.observe(viewLifecycleOwner, Observer { commonResource ->
            activity?.findViewById<SwipeRefreshLayout>(R.id.srl_threads)?.isRefreshing = false

            commonResource?.extractData?.let { list ->
                //Log.d(TAG, "lastRequest brdid: ${lastRequest?.brdid} actual brdid ${it.first().brdid}")
                if (list.isNotEmpty() && requestThreadsLayoutRefresh) {
                    requestThreadsLayoutRefresh = false
                    adapter?.submitList(list)
                    if (scrollToLastPos) {
                        lastRequest?.thrdid?.let { thrdid ->
                            val pos = adapter?.getPosFromId(thrdid.toLong()) ?: 0
                            if (pos > 0) (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(pos, 500)
                        }
                    }
                    // delay load for smoother animation
                    if (requestOpenPane) {
                        requestOpenPane = false
                        Handler().postDelayed({ slidingPaneLayout?.openPane()}, 600)
                    }
                }
            }
        })

        component.mainVM.getLatestRequestItem().observe(viewLifecycleOwner, Observer { requestItem ->
            requestItem?.let {
                Log.i(TAG, "latest msgId: ${it.msgid}")
                it.thrdid?.let {thrdid -> adapter?.setLastSelected(null, thrdid.toLong()) }
                lastRequest = it
               }
        })

        //swipe to refresh
        activity?.findViewById<SwipeRefreshLayout>(R.id.srl_threads)?.setOnRefreshListener{
            scrollToLastPos = false
            if (lastRequest != null) {
                FuncFetch.fetchThreads(component.mainVM, component.mainDS, lastRequest!!, false)
                        .subscribe ({   }, { alert(it) })
                        .disposeOn(compositeDisposable)
            }
        }

    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.day_night -> {
                component.defaultSettings.edit().putBoolean(C_SETTINGS.NIGHT_MODE, !component.defaultSettings.getBoolean(C_SETTINGS.NIGHT_MODE, true)).apply()
                activity?.recreate()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDeepLink(deeplink: String) {
        Log.i(TAG, "deeplink $deeplink")
        compositeDisposable.add(component.mainDS.parseManiacUrl(deeplink)
                .filter { it.brdid != -1 && it.thrdid != -1 }
                .subscribeOn(Schedulers.io())

                .observeOn(AndroidSchedulers.mainThread())
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
        if (requestItem.msgid != null) component.mainVM.setMessageRequestItem(requestItem)
        FuncFetch.fetchThreads(component.mainVM, component.mainDS, requestItem, true)
                .subscribe ({   }, { alert(it) })
                .disposeOn(compositeDisposable)
        FuncFetch.fetchReplies(component.mainVM, component.mainDS, requestItem)
                .subscribe ({   }, { alert(it) })
                .disposeOn(compositeDisposable)
    }

    private fun initBoardSpinner(requestItem: LoadRequestItem?) {
        val spinner: Spinner? = activity?.findViewById(R.id.sp_boards)
        val oldList = component.defaultSettings.getString(C_SETTINGS.BOARDS, emptyList<Board>().toJsonString())?.toJsonObjectList<Board>()
        setSpinner(spinner, oldList!!, requestItem)
    }

    private fun updateBoards(loadRequestItem: LoadRequestItem) {
        component.mainVM.getBoardsLiveDataRx()?.observeOnce(Observer {
            it?.extractData?.let { list ->
                val spinner: Spinner? = activity?.findViewById(R.id.sp_boards)
                setSpinner(spinner, list, loadRequestItem)
                component.defaultSettings.edit().putString(C_SETTINGS.BOARDS, list.toJsonString()).apply()
            }
        })
    }

    private fun setSpinner(spinner: Spinner?, boardList: List<Board>, requestItem: LoadRequestItem?) {
        spinner?.adapter = ArrayAdapter(activity as Context, R.layout.main_spinner, boardList.map { it.label })
        if (boardList.map { it.brdid }.contains(requestItem?.brdid)) {
            Log.d(TAG, "spinner set true")
            spinner?.setSelection(boardList.map { it.brdid }.indexOf(requestItem?.brdid))
        }

        // only load if user selects board manually
        spinner?.setOnTouchListener { _, _ ->
            loadBoard = true
            return@setOnTouchListener false
        }

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                requestThreadsLayoutRefresh = true
                if (loadBoard) {
                    loadBoard = false
                    requestOpenPane = true
                    val loadThreadsRequest = LoadRequestItem(boardList[position].brdid, null, null)

                    FuncFetch.fetchThreads(component.mainVM, component.mainDS, loadThreadsRequest, true)
                            .subscribe ({  component.mainVM.setThreadsRequestItem(loadThreadsRequest) }, { alert(it) })
                            .disposeOn(compositeDisposable)

                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }


    override fun onDetach() {
        compositeDisposable.clear()
        super.onDetach()
    }

    override fun onStop() {
        lastRequest?.also { request ->
            component.defaultSettings.edit().putString(C_SETTINGS.PREVIOUS_REQUEST, request.toJsonString()).apply()
        }
        super.onStop()
    }

    companion object {
        private const val TAG = "MainThreadsFrag"
    }
}
