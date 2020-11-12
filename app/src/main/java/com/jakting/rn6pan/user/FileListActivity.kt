package com.jakting.rn6pan.user

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.adapter.FileListAdapter
import com.jakting.rn6pan.api.data.FileOrDirectoryList
import com.jakting.rn6pan.utils.*
import com.jakting.rn6pan.utils.MyApplication.Companion.ctimeOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.defaultOrder
import com.jakting.rn6pan.utils.MyApplication.Companion.nameOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.nowPath
import com.jakting.rn6pan.utils.MyApplication.Companion.orderFlag
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_file_list.*
import okhttp3.MediaType
import okhttp3.RequestBody


class FileListActivity : BaseActivity() {

    companion object {
        lateinit var nowFileOrDirectoryList: FileOrDirectoryList
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_file_list)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.file_toolbar_title)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initBottomBarNavIcon()
        //下拉刷新
        file_list_swipeLayout.apply {
            setEnableRefresh(true)
            autoRefresh()
            setPrimaryColorsId(R.color.colorAccent, R.color.colorPrimary)
        }
        file_list_swipeLayout.setOnRefreshListener {
            initFileOrDirectoryList()
        }
    }


    private fun initFileOrDirectoryList() {
        val jsonForPost =
            "{\"nowPath\":\"$nowPath\",\"orderby\":[" +
                    (if (!defaultOrder) {
                        (if (orderFlag == 0)
                            (if (nameOrderBy != "") "[\"name\",\"$nameOrderBy\"]" else "")
                        else
                            (if (ctimeOrderBy != "") "[\"ctime\",\"$ctimeOrderBy\"]" else ""))
                    } else "") +
                    "]}"
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"), jsonForPost
            )
        val observable =
            EncapsulateRetrofit.init().getFileOrDirectoryList(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fileOrDirectoryList ->
                logd("onNext // getFileOrDirectoryList")
                nowFileOrDirectoryList = fileOrDirectoryList
                setAdapter()
            }) { t ->
                logd("onError // getFileOrDirectoryList")
                t.printStackTrace()
            }
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(this)
        file_list_recyclerView.layoutManager = layoutManager
        val isRoot = nowPath == ""
        val adapter = FileListAdapter(nowFileOrDirectoryList.dataList, isRoot)
        file_list_recyclerView.adapter = adapter
        file_list_swipeLayout.finishRefresh(1000)
    }

    private fun initBottomBarNavIcon() {
        bottomAppBar.setNavigationOnClickListener {
            val items = arrayOf(
                getString(R.string.file_sort_default),
                getString(R.string.file_sort_filename_asc),
                getString(R.string.file_sort_filename_desc),
                getString(R.string.file_sort_ctime_asc),
                getString(R.string.file_sort_ctime_desc),
            )
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.file_sort_title))
                .setItems(items) { dialog, which ->
                    when (which) {
                        0 -> { //恢复默认排序
                            defaultOrder = true
                            file_list_swipeLayout.autoRefresh()
                        }
                        1 -> { //按文件名正序
                            nameOrderBy = "asc"
                            orderFlag = 0
                            defaultOrder = false
                            file_list_swipeLayout.autoRefresh()
                        }
                        2 -> { //按文件名倒序
                            nameOrderBy = "desc"
                            orderFlag = 0
                            defaultOrder = false
                            file_list_swipeLayout.autoRefresh()
                        }
                        3 -> { //按创建时间正序
                            ctimeOrderBy = "asc"
                            orderFlag = 1
                            defaultOrder = false
                            file_list_swipeLayout.autoRefresh()
                        }
                        4 -> { //按创建时间倒序
                            ctimeOrderBy = "desc"
                            orderFlag = 1
                            defaultOrder = false
                            file_list_swipeLayout.autoRefresh()
                        }
                    }
                }
                .show()
        }
    }
}