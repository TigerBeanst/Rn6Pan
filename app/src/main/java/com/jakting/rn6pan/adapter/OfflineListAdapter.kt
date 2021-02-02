package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.user.OfflineListActivity
import com.jakting.rn6pan.api.data.OfflineFile
import com.jakting.rn6pan.utils.MyApplication.Companion.appContext
import com.jakting.rn6pan.utils.getPrintSize
import com.jakting.rn6pan.utils.toast


class OfflineListAdapter(
    private val offlineList: List<OfflineFile>,
    private val activity: OfflineListActivity
) :
    RecyclerView.Adapter<OfflineListAdapter.ViewHolder>() {
    lateinit var parentContext: Context

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileOrDirectoryLayout: LinearLayout = view.findViewById(R.id.file_list_layout)
        val fileOrDirectoryLabel: LinearLayout = view.findViewById(R.id.file_list_filelabel)
        val fileOrDirectoryIcon: ImageView = view.findViewById(R.id.file_list_fileicon)
        val fileOrDirectoryName: TextView = view.findViewById(R.id.file_list_filename)
        val fileOrDirectoryInfo: TextView = view.findViewById(R.id.file_list_info)
        val fileOrDirectoryMoreButton: ImageButton = view.findViewById(R.id.file_list_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        parentContext = parent.context
        val view =
            LayoutInflater.from(parentContext)
                .inflate(R.layout.item_file_or_directory, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offlineFile = offlineList[position]
        holder.fileOrDirectoryLabel.setBackgroundColor(Color.TRANSPARENT)
        when (offlineFile.type) {
            //还在收集中
            5060 -> holder.fileOrDirectoryIcon.setImageDrawable(
                ContextCompat.getDrawable(appContext, R.drawable.file_icon_video)
            )//mp4
            else -> holder.fileOrDirectoryIcon.setImageDrawable(
                ContextCompat.getDrawable(appContext, R.drawable.file_icon_unknown)
            )
        }

        holder.fileOrDirectoryMoreButton.setImageDrawable(
            when (offlineFile.status) {
                in -999..0, in 301..999 -> ContextCompat.getDrawable(
                    parentContext,
                    R.drawable.ic_baseline_cloud_off_24
                )
                1000 -> ContextCompat.getDrawable(
                    parentContext,
                    R.drawable.ic_baseline_cloud_done_24
                )
                else -> ContextCompat.getDrawable(
                    parentContext,
                    R.drawable.ic_baseline_cloud_24
                )
            }
        )
        holder.fileOrDirectoryName.apply {
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint = offlineFile.name
        }
        val params = holder.fileOrDirectoryInfo.layoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.fileOrDirectoryLayout.setOnLongClickListener {
            toast(getStatusCodeDetail(offlineFile.status))
            true
        }
        holder.fileOrDirectoryMoreButton.setOnClickListener {
            toast(getStatusCodeDetail(offlineFile.status))
        }
        holder.fileOrDirectoryInfo.apply {
            layoutParams = params
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint = if (offlineFile.progress == 100) "下载完成" else {
                offlineFile.progress.toString() + "%"
            } +
                    " (" + getPrintSize(offlineFile.processedSize) + "/" + getPrintSize(offlineFile.size) + ")"
        }
    }

    override fun getItemCount() = offlineList.size

    private fun getStatusCodeDetail(status: Int): String =
        parentContext.getString(R.string.offline_item_task_status) +
                when (status) {
                    1000 -> "下载完成"
                    -100 -> "下载超时"
                    -200 -> "下载被禁止"
                    -300 -> "用户空间不足"
                    -400 -> "用户账户问题"
                    -500 -> "文件过大"
                    -510 -> "子文件过多"
                    100 -> "等待数据传输或复制"
                    1301 -> "境外节点正在下载"
                    200 -> "数据已经下载到服务器，等待复制到用户空间"
                    11303 -> "下载队列发生堵塞"
                    300 -> "正在远程下载文件"
                    302 -> "文件太大，进入缓慢下载队列"
                    303 -> "服务器磁盘空间不足"
                    306 -> "文件无法下载，1月后系统自动尝试重试"
                    307 -> "文件无法下载，1月后系统自动尝试重试"
                    309 -> "文件下载缓慢，持续12小时无法获取meta或者无进度"
                    310 -> "文件下载缓慢，文件地址可能存在问题 或 （百度）文件子文件太多或者大小超限"
                    400 -> "文件地址存在问题"
                    406 -> "文件无法下载，1周后系统自动尝试重试"
                    407 -> "文件无法下载，1周后系统自动尝试重试"
                    else -> "其他未知错误"
                }


}