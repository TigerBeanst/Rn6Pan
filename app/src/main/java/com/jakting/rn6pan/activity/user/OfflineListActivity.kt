package com.jakting.rn6pan.activity.user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.user.offline.OfflineByLinksActivity
import com.jakting.rn6pan.adapter.OfflineListAdapter
import com.jakting.rn6pan.api.accessAPI
import com.jakting.rn6pan.api.data.OfflineClear
import com.jakting.rn6pan.api.data.OfflineClearDeleteReturn
import com.jakting.rn6pan.api.data.OfflineList
import com.jakting.rn6pan.utils.getPostBody
import com.jakting.rn6pan.utils.logd
import com.jakting.rn6pan.utils.toast
import kotlinx.android.synthetic.main.activity_user_offline_list.*


class OfflineListActivity : BaseActivity() {
    var nowOnPage = 0
    lateinit var offlineListAdapter: OfflineListAdapter

    companion object {
        lateinit var nowOfflineList: OfflineList
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_offline_list_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishAfterTransition()
            R.id.menu_offline_toolbar_delete_all -> {
//                clickMenuStar()
            }
            R.id.menu_offline_toolbar_delete_finished -> {
//                clickMenuStar()
            }
            R.id.menu_offline_toolbar_delete_unfinished -> {
//                clickMenuStar()
            }
            R.id.menu_offline_toolbar_delete_error -> {
//                clickMenuStar()
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_offline_list)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(R.string.offline_toolbar_title)
        offline_fab.setOnClickListener {
            clickFAB()
        }
        //下拉刷新
        offline_list_swipeLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setEnableAutoLoadMore(true)
            setEnableScrollContentWhenLoaded(true)
            setPrimaryColorsId(R.color.colorAccent, R.color.colorPrimary)
            autoRefresh()
        }
        offline_list_swipeLayout.setOnRefreshListener {
            nowOnPage = 0
            initOfflineList(false)
        }
        offline_list_swipeLayout.setOnLoadMoreListener {
            nowOnPage++
            initOfflineList(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 1 && data?.getBooleanExtra("isSubmit", false)!!) {
            offline_list_swipeLayout.autoRefresh()
        }
    }

    /**
     * 初始化离线列表
     * @param isLoadMore Boolean
     */
    private fun initOfflineList(isLoadMore: Boolean) {
        val jsonForPost =
            "{\"skip\":${nowOnPage * 20}," +
                    "\"limit\":20," +
                    "\"orderby\": [[\"create_time\", \"desc\"]]" +
                    "}"
        accessAPI(
            {
                getOfflineList(getPostBody(jsonForPost))
            }, { objectReturn ->
                val offlineList = objectReturn as OfflineList
                logd("onNext // initOfflineList")
                if (isLoadMore) {
                    nowOfflineList.dataList += offlineList.dataList
                } else {
                    nowOfflineList = offlineList
                }
                setOfflineListAdapter()
            }) {
            toast(getString(R.string.action_fail))
        }
    }

    /**
     * 清空离线任务列表
     * @param type Int
     * @param deleteFile Boolean
     */
    private fun clearOfflineTask(type: Int, deleteFile: Boolean) {
        val typeString = when (type) {
            10000 -> getString(R.string.offline_toolbar_clear_all)
            1000 -> getString(R.string.offline_toolbar_clear_finished)
            100 -> getString(R.string.offline_toolbar_clear_unfinished)
            -100 -> getString(R.string.offline_toolbar_clear_error)
            else -> getString(R.string.offline_toolbar_clear_unknown)
        }
        if (typeString != getString(R.string.offline_toolbar_clear_unknown)) {
            val jsonForPost =
                "{\"type\":$type,\"deleteFile\":$deleteFile }"
            accessAPI(
                {
                    clearOfflineTask(getPostBody(jsonForPost))
                }, { objectReturn ->
                    val offlineClearDeleteReturn = objectReturn as OfflineClearDeleteReturn
                    logd("onNext // clearOfflineTask")
                    if (offlineClearDeleteReturn.successCount == 1) {
                        if (deleteFile) { //顺带文件删除了
                            toast(
                                String.format(
                                    getString(R.string.offline_toolbar_delete_file_ok),
                                    typeString
                                )
                            )
                        } else { //没有删除文件
                            toast(
                                String.format(
                                    getString(R.string.offline_toolbar_delete_file_ok_without_file),
                                    typeString
                                )
                            )
                        }
                    } else {
                        toast(getString(R.string.action_fail))
                    }
                    setOfflineListAdapter()
                }) {
                toast(getString(R.string.action_fail))
            }
        } else {
            toast(getString(R.string.offline_toolbar_clear_unknown))
        }

    }

    /**
     * 设置离线列表适配器
     */
    private fun setOfflineListAdapter() {
        val layoutManager = LinearLayoutManager(this)
        offline_list_recyclerView.layoutManager = layoutManager
        offlineListAdapter = OfflineListAdapter(nowOfflineList.dataList, this)
        offline_list_recyclerView.adapter = offlineListAdapter
        offline_list_recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (offline_fab.isShown) {
                        offline_fab.hide()
                    }
                } else {
                    if (!offline_fab.isShown) {
                        offline_fab.show()
                    }
                }
            }
        })
        offline_list_swipeLayout.finishRefresh(0)
        offline_list_swipeLayout.finishLoadMore(0)
    }

    /**
     * 初始化 FAB
     */
    private fun clickFAB() {
        val items = arrayOf(
            getString(R.string.offline_new_by_links),
            getString(R.string.offline_new_by_bt),
            getString(R.string.offline_new_by_browser)
        )
        val dialogBy = MaterialAlertDialogBuilder(this)
        dialogBy.setTitle(resources.getString(R.string.offline_new_title))
            .setItems(items) { dialog, which ->
                lateinit var intent: Intent
                when (which) {
                    0 -> {
                        intent = Intent(this, OfflineByLinksActivity::class.java)
                        startActivityForResult(intent, 1)
                    }
                    1 -> {
//                        intent = Intent(this, OfflineByLinksActivity::class.java)
                    }
                    2 -> {
//                        intent = Intent(this, OfflineByLinksActivity::class.java)
                    }
                }
            }
            .show()

    }


}