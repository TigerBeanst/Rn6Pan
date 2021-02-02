package com.jakting.rn6pan.activity.common

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.adapter.ChoosePathListAdapter
import com.jakting.rn6pan.api.accessAPI
import com.jakting.rn6pan.api.data.FileOrDirectoryList
import com.jakting.rn6pan.utils.MyApplication.Companion.parentChoosePathList
import com.jakting.rn6pan.utils.getPostBody
import com.jakting.rn6pan.utils.logd
import com.jakting.rn6pan.utils.toast
import kotlinx.android.synthetic.main.activity_choose_path.*


class ChoosePathActivity : BaseActivity() {
    var nowOnPage = 0
    lateinit var choosePathListAdapter: ChoosePathListAdapter

    companion object {
        lateinit var nowDirectoryList: FileOrDirectoryList
        var isUpToParentPath = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_path)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        parentChoosePathList = arrayListOf<String>("/")
        initButton()
        //下拉刷新
        choose_path_swipeLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setEnableAutoLoadMore(true)
            setEnableScrollContentWhenLoaded(true)
            setPrimaryColorsId(R.color.colorAccent, R.color.colorPrimary)
            autoRefresh()
        }
        choose_path_swipeLayout.setOnRefreshListener {
            if (isUpToParentPath) {
                parentChoosePathList.removeAt(parentChoosePathList.size - 1)
                isUpToParentPath = false
            }
            nowOnPage = 0
            initChoosePathList(false)
        }
        choose_path_swipeLayout.setOnLoadMoreListener {
            nowOnPage++
            initChoosePathList(true)
        }
    }

    /**
     * 初始化 FAB
     */
    private fun initButton() {
        choose_path_fab.setOnClickListener {
            val intent = Intent()
            intent.putExtra("choose_path",parentChoosePathList[parentChoosePathList.size - 1])
            setResult(1,intent)
            finishAfterTransition()
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        choose_path_back.setOnClickListener{
            super.onBackPressed()
        }
    }

    /**
     * 初始化文件夹列表
     */
    private fun initChoosePathList(isLoadMore: Boolean) {
        val jsonForPost =
            "{\"parentPath\":\"${parentChoosePathList[parentChoosePathList.size - 1]}\"," +
                    "\"skip\": ${nowOnPage * 20}," +
                    "\"limit\":20,\"directory\":true}"
        accessAPI(
            {
                getFileOrDirectoryList(getPostBody(jsonForPost))
            }, { objectReturn ->
                val directoryList = objectReturn as FileOrDirectoryList
                logd("onNext // initChoosePathList")
                if (isLoadMore) {
                    nowDirectoryList.dataList += directoryList.dataList
                } else {
                    nowDirectoryList = directoryList
                }
                setChoosePathListAdapter()
            }) {
            toast(getString(R.string.action_fail))
        }
    }

    /**
     * 设置文件列表适配器
     */
    private fun setChoosePathListAdapter() {
        val layoutManager = LinearLayoutManager(this)
        choose_path_recyclerView.layoutManager = layoutManager
        choosePathListAdapter = ChoosePathListAdapter(nowDirectoryList.dataList, this)
        if (parentChoosePathList.size > 1) {
            if (supportActionBar != null) {
                supportActionBar!!.title = nowDirectoryList.parent.name
            }
        } else {
            if (supportActionBar != null) {
                supportActionBar!!.title = getString(R.string.offline_new_by_links_path_toolbar)
            }
        }
        choose_path_recyclerView.adapter = choosePathListAdapter
        choose_path_recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (choose_path_fab.isShown) {
                        choose_path_fab.hide()
                    }
                } else {
                    if (!choose_path_fab.isShown) {
                        choose_path_fab.show()
                    }
                }
            }
        })
        choose_path_swipeLayout.finishRefresh(0)
        choose_path_swipeLayout.finishLoadMore(0)
    }

    /**
     * 回退到上一个文件夹
     */
    private fun backToParentPath() {
        isUpToParentPath = true
        choose_path_swipeLayout.autoRefresh()
    }

    override fun onBackPressed() {
        if (parentChoosePathList.size == 1) {
            super.onBackPressed()
        } else {
            nowOnPage = 0
            backToParentPath()
        }
    }

}