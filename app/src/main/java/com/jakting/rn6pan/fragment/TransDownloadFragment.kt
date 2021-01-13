package com.jakting.rn6pan.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.arialyy.annotations.Download
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.AbsEntity
import com.arialyy.aria.core.task.DownloadTask
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.user.FileListActivity
import com.jakting.rn6pan.adapter.DownloadListAdapter
import com.jakting.rn6pan.utils.logd
import kotlinx.android.synthetic.main.fragment_trans_download.*

// TODO: Rename parameter arguments, choose names that match
class TransDownloadFragment : Fragment() {

    private val mDownloadData: MutableList<AbsEntity> = ArrayList()
    lateinit var downloadListAdapter: DownloadListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Aria.download(this).register() //初始化 Aria 下载引擎
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trans_download, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initFragment()
    }

    private fun initFragment() {
        val temps = Aria.download(this).taskList
//        val temps = Aria.download(this).totalTaskList
        if (temps != null && temps.isNotEmpty()) {
            for (temp in temps) {
                logd("state = " + temp.state)
            }
            mDownloadData.addAll(temps)
            val layoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = layoutManager
            downloadListAdapter = DownloadListAdapter(temps, activity as FileListActivity)
            recyclerView.adapter = downloadListAdapter

        } else {
            logd("temps为null")
        }
    }

    //下载中
    @Download.onTaskRunning
    fun taskRunning(task: DownloadTask) {
        if (recyclerView.adapter != null) {
            (recyclerView.adapter as DownloadListAdapter).setProgress(task.entity)
        }
    }

    //下载完成
    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        if (recyclerView.adapter != null) {
            (recyclerView.adapter as DownloadListAdapter).setProgress(task.entity)
        }
    }
}