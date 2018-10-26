package com.kyoapps.maniac.ui

import androidx.lifecycle.Observer
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.kyoapps.maniac.helpers.classes.subOnNewObsOnMain
import com.kyoapps.maniac.ui.adapters.MainThreadsAdapter
import com.squareup.moshi.Moshi
import io.reactivex.disposables.CompositeDisposable
import android.widget.ArrayAdapter
import android.widget.Toast
import com.kyoapps.maniac.helpers.classes.observeOnce
import com.kyoapps.maniac.helpers.classes.pojo.Board
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types


class MainThreadsFrag : androidx.fragment.app.Fragment() {

    private lateinit var component: ActivityComponent
    private lateinit var compositeDisposable: CompositeDisposable
    private var lastRequest: LoadRequestItem? = null
    private var requestThreadsLayoutRefresh = true
    private var loadBoard = false
    private var requestOpenPane = false
    private var scrollToLastPos = true

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
        lastRequest = Moshi.Builder().build().adapter(LoadRequestItem::class.java).fromJson(
                component.defaultSettings.getString(C_SETTINGS.PREVIOUS_REQUEST,
                        Moshi.Builder().build().adapter(LoadRequestItem::class.java).toJson(LoadRequestItem(1, 168250, 4224606))))

        Log.d(TAG, "savedInstanceState == null: ${savedInstanceState == null}")
        if (savedInstanceState == null) {
            // load deeplink if present. Else load last opened
            Toast.makeText(context, "SIS null; lastRequest: $lastRequest", Toast.LENGTH_LONG).show()
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
            Toast.makeText(context, "SIS not null; lastRequest: $lastRequest", Toast.LENGTH_LONG).show()
        }


        val recyclerView: androidx.recyclerview.widget.RecyclerView = view.findViewById(R.id.rv_threads)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)

        val slidingPaneLayout: androidx.slidingpanelayout.widget.SlidingPaneLayout? = activity?.findViewById(R.id.pane_main)

        @ColorInt val colorPressed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context?.resources?.getColor(R.color.grey_trans_2, activity?.theme)?: Color.GRAY
        } else { context?.resources?.getColor(R.color.grey_trans_2)?: Color.GRAY }

        val adapter: MainThreadsAdapter? = MainThreadsAdapter(context, slidingPaneLayout, component as DaggerActivityComponent)

        recyclerView.adapter = adapter

        component.mainVM.threadsLiveDataRx()?.observe(this, Observer { commonResource ->
<<<<<<< HEAD
            activity?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.srl_threads)?.isRefreshing = false
=======
            activity?.findViewById<SwipeRefreshLayout>(R.id.srl_threads)?.isRefreshing = false
>>>>>>> 8a1e94693e63922ed0fd786654e66d081f7a6a63

            commonResource?.data?.let { list ->
                //Log.d(TAG, "lastRequest brdid: ${lastRequest?.brdid} actual brdid ${it.first().brdid}")
                if (list.isNotEmpty() && requestThreadsLayoutRefresh) {
                    requestThreadsLayoutRefresh = false
                    adapter?.submitList(list)
                    if (scrollToLastPos) {
                        lastRequest?.thrdid?.let { thrdid ->
                            val pos = adapter?.getPosFromId(thrdid.toLong()) ?: 0
<<<<<<< HEAD
                            if (pos > 0) (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).scrollToPositionWithOffset(pos, 500)
=======
                            if (pos > 0) (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(pos, 500)
>>>>>>> 8a1e94693e63922ed0fd786654e66d081f7a6a63
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

        component.mainVM.getLatestRequestItem().observe(this, Observer { requestItem ->
            requestItem?.let {
                Log.i(TAG, "latest msgId: ${it.msgid}")
                it.thrdid?.let {thrdid -> adapter?.setLastSelected(null, thrdid.toLong()) }
                lastRequest = it
               }
        })

        //swipe to refresh
<<<<<<< HEAD
        activity?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.srl_threads)?.setOnRefreshListener{
=======
        activity?.findViewById<SwipeRefreshLayout>(R.id.srl_threads)?.setOnRefreshListener{
>>>>>>> 8a1e94693e63922ed0fd786654e66d081f7a6a63
            scrollToLastPos = false
            if (lastRequest != null) FuncFetch.fetchThreads(component.mainVM, component.mainDS, lastRequest!!, false)
        }

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

    private fun loadDeepLink(deeplink: String) {
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
        if (requestItem.msgid != null) component.mainVM.setMessageRequestItem(requestItem)
        FuncFetch.fetchThreads(component.mainVM, component.mainDS, requestItem, true)
        FuncFetch.fetchReplies(component.mainVM, component.mainDS, requestItem)
    }

    private fun initBoardSpinner(requestItem: LoadRequestItem?) {
        val spinner: Spinner? = activity?.findViewById(R.id.sp_boards)
        val moshiAdapter: JsonAdapter<List<Board>> = Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, Board::class.java))
        val oldList = moshiAdapter.fromJson(component.defaultSettings.getString(C_SETTINGS.BOARDS, moshiAdapter.toJson(ArrayList(0))))
        setSpinner(spinner, oldList!!, requestItem)
    }

    private fun updateBoards(loadRequestItem: LoadRequestItem) {
        component.mainVM.getBoardsLiveDataRx()?.observeOnce(this, Observer {
            it?.data?.let { list ->
                val spinner: Spinner? = activity?.findViewById(R.id.sp_boards)
                setSpinner(spinner, list, loadRequestItem)
                val moshiAdapter: JsonAdapter<List<Board>> = Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, Board::class.java))
                component.defaultSettings.edit().putString(C_SETTINGS.BOARDS, moshiAdapter.toJson(list)).apply()
            }
        })
    }

    private fun setSpinner(spinner: Spinner?, boardList: List<Board>, requestItem: LoadRequestItem?) {
        spinner?.adapter = ArrayAdapter(activity, R.layout.main_spinner, boardList.map { it.label })
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
                    component.mainVM.setThreadsRequestItem(loadThreadsRequest)
                }
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
