package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadEntity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.user.FileListActivity
import com.jakting.rn6pan.utils.database.DownloadListTable
import com.jakting.rn6pan.utils.toast
import org.litepal.LitePal
import org.litepal.extension.deleteAll


class DownloadListAdapter(
    private val downloadList: List<DownloadEntity>,
    private val activity: FileListActivity
) :
    RecyclerView.Adapter<DownloadListAdapter.ViewHolder>() {
    lateinit var parentContext: Context
    private var downloadListModify: MutableList<DownloadEntity> =
        downloadList as MutableList<DownloadEntity>

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {
        val fileOrDirectoryName: TextView = view.findViewById(R.id.file_list_filename)
        val fileOrDirectoryInfo: TextView = view.findViewById(R.id.file_list_info)
        val fileOrDirectoryMoreButton: ImageButton = view.findViewById(R.id.file_list_more)

        init {
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        parentContext = parent.context
        val view =
            LayoutInflater.from(parentContext)
                .inflate(R.layout.item_download, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileOrDirectory = downloadListModify[position]
        holder.itemView.setTag(R.id.item_position, position)
        holder.itemView.setTag(R.id.item_fileOrDirectory, fileOrDirectory)
        holder.itemView.setTag(R.id.item_viewholder, holder)
        holder.itemView.setOnClickListener {
            when (downloadListModify[position].state) {
                2 -> Aria.download(this).load(downloadListModify[position].id)
                    .ignoreCheckPermissions().resume()//暂停中，要进行
                4 -> Aria.download(this).load(downloadListModify[position].id)
                    .ignoreCheckPermissions().stop()//进行中，要暂停
            }
        }
        holder.fileOrDirectoryMoreButton.setOnClickListener {
            val popup = PopupMenu(parentContext, holder.fileOrDirectoryMoreButton)
            popup.inflate(R.menu.menu_transmission_download_item)
            popup.setOnMenuItemClickListener {
                //点击更多
                when (it.itemId) {
                    R.id.menu_transmission_download_delete -> {
                        Aria.download(this).load(downloadListModify[position].id)
                            .ignoreCheckPermissions().cancel(true)
                        LitePal.deleteAll<DownloadListTable>("fileTaskId = ?",downloadListModify[position].id.toString())
                    }
                    R.id.menu_transmission_download_open_with -> {
                        toast("test")
                    }
                }
                true
            }
            popup.show()
        }
        holder.fileOrDirectoryName.apply {
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint = downloadListModify[position].fileName
        }
        val params = holder.fileOrDirectoryInfo.layoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.fileOrDirectoryInfo.apply {
            layoutParams = params
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint = if (downloadListModify[position].isComplete) {
                parentContext.getString(R.string.transmission_download_finish)
            } else {
                downloadListModify[position].percent.toString() + "% " + if (downloadListModify[position].convertSpeed != null) downloadListModify[position].convertSpeed else ""
            }
        }
    }

    //动态设置任务下载进度
    @Synchronized
    fun updateProgress(entity: DownloadEntity) {
        val url = entity.key
        val position: Int = indexItem(url)
        if (position == -1 || position >= downloadListModify.size) {
            return
        }
        downloadListModify[position] = entity
        notifyItemChanged(position, entity)
    }

    //找寻对应下载任务的位置
    @Synchronized
    private fun indexItem(url: String): Int {
        for (downloadIndex in downloadListModify.indices) {
            if (downloadListModify[downloadIndex].url == url) {
                return downloadIndex
            }
        }
        return -1
    }


    override fun getItemCount() = downloadListModify.size

}