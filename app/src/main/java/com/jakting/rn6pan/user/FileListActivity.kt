package com.jakting.rn6pan.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.adapter.FileListAdapter
import com.jakting.rn6pan.utils.EncapsulateRetrofit
import com.jakting.rn6pan.utils.FileOrDirectory
import com.jakting.rn6pan.utils.FileOrDirectoryList
import com.jakting.rn6pan.utils.MyApplication.Companion.ctimeOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.directoryOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.nameOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.nowPath
import com.jakting.rn6pan.utils.MyApplication.Companion.typeOrderBy
import com.jakting.rn6pan.utils.logd
import com.scwang.smart.refresh.header.MaterialHeader
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
        refreshLayout.setRefreshHeader(MaterialHeader(this))
        refreshLayout.setEnableRefresh(true)
        refreshLayout.autoRefresh()
        refreshLayout.setOnRefreshListener { refreshlayout ->
            initFileOrDirectoryList()
        }
        refreshLayout.setOnLoadMoreListener { refreshlayout ->
            refreshlayout.finishLoadMore(500 /*,false*/) //传入false表示加载失败
        }
    }


    private fun initFileOrDirectoryList() {
        val jsonForPost =
            "{\"nowPath\":\"$nowPath\",\"orderby\":[" +
                    if (directoryOrderBy != "") "[\"directoryOrderBy\",\"$directoryOrderBy\"]," else "" +
                            if (nameOrderBy != "") "[\"name\",\"$nameOrderBy\"]" else "" +
                                    if (typeOrderBy != "") "[\"type\",\"$typeOrderBy\"]" else "" +
                                            if (ctimeOrderBy != "") "[\"ctime\",\"$ctimeOrderBy\"]," else "" +
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
                logd("onNext // checkDestination")
                nowFileOrDirectoryList = fileOrDirectoryList
                setAdapter()
            }) { t ->
                logd("onError // checkDestination")
                t.printStackTrace()
            }
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(this)
        file_list_recyclerView.layoutManager = layoutManager
        val isRoot = nowPath==""
        val adapter = FileListAdapter(nowFileOrDirectoryList.dataList, isRoot)
        file_list_recyclerView.adapter = adapter
        refreshLayout.finishRefresh(500 /*,false*/) //传入false表示刷新失败
    }
}