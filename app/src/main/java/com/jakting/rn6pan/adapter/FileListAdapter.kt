package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakting.rn6pan.R
import com.jakting.rn6pan.utils.FileOrDirectory
import com.jakting.rn6pan.utils.MyApplication
import com.jakting.rn6pan.utils.MyApplication.Companion.userInfo
import com.jakting.rn6pan.utils.getPrintSize
import com.jakting.rn6pan.utils.logd
import com.maning.imagebrowserlibrary.ImageEngine
import com.maning.imagebrowserlibrary.MNImageBrowser
import kotlinx.android.synthetic.main.content_file_info.view.*
import java.text.SimpleDateFormat


class FileListAdapter(val fileOrDirectoryList: List<FileOrDirectory>, val isRoot: Boolean) :
    RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileOrDirectoryIcon: ImageView = view.findViewById(R.id.file_list_fileicon)
        val fileOrDirectoryName: TextView = view.findViewById(R.id.file_list_filename)
        val fileOrDirectoryInfo: TextView = view.findViewById(R.id.file_list_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_file_or_directory, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener{
            val position = viewHolder.adapterPosition
            val fileOrDirectory = fileOrDirectoryList[position]
            if (fileOrDirectory.directory){

            }else if(fileOrDirectory.mime.contains("video")){
                //播放视频
                if(userInfo.vip!=0){ //已订阅

                }else{ //未订阅
                    MaterialAlertDialogBuilder(parent.context)
                        .setTitle(parent.context.getString(R.string.file_video_notvip))
                        .setMessage(parent.context.getString(R.string.file_video_notvip_desc))
                        .setPositiveButton(parent.context.getString(R.string.ok)) {_,_->}
                        .show()
                }
            }else if(fileOrDirectory.mime.contains("image")){
                //图片预览

                MNImageBrowser.with(parent.context)
                    .setCurrentPosition(position)
                    //必须-图片加载用户自己去选择
                    .setImageEngine(com.jakting.rn6pan.`interface`.ImageEngine.GlideImageEngine() as ImageEngine)
                    //必须（setImageList和setImageUrl二选一，会覆盖）-图片集合
                    .setImageUrl("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png")
            }
        }
        viewHolder.itemView.setOnLongClickListener{
            val position = viewHolder.adapterPosition
            val fileOrDirectory = fileOrDirectoryList[position]
            initBottomDialog(fileOrDirectory, parent)
            true
        }
        return viewHolder
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileOrDirectory = fileOrDirectoryList[position]
        if (fileOrDirectory.directory) {
            holder.fileOrDirectoryIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    MyApplication.appContext,
                    R.drawable.file_icon_directory
                )
            )
        } else {
            if (fileOrDirectory.mime.contains("video")) {
                holder.fileOrDirectoryIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        MyApplication.appContext,
                        R.drawable.file_icon_video
                    )
                )
            } else if (fileOrDirectory.mime.contains("application/zip")) {
                holder.fileOrDirectoryIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        MyApplication.appContext,
                        R.drawable.file_icon_zip
                    )
                )
            } else {
                holder.fileOrDirectoryIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        MyApplication.appContext,
                        R.drawable.file_icon_unknown
                    )
                )
            }
        }
        holder.fileOrDirectoryName.hint = fileOrDirectory.name
        holder.fileOrDirectoryInfo.hint =
            getPrintSize(fileOrDirectory.size) + " " + SimpleDateFormat("YYYY/MM/dd").format(
                fileOrDirectory.ctime
            )
    }

    override fun getItemCount() = fileOrDirectoryList.size

    private fun initBottomDialog(fileOrDirectory: FileOrDirectory, parent: ViewGroup){
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.content_file_info, null)
        view.file_one_filename.text = fileOrDirectory.name
        view.file_one_size.text = getPrintSize(fileOrDirectory.size)
        val dialog = BottomSheetDialog(view.context)
        dialog.setContentView(view.file_one_dialog)
        dialog.show()
        logd("触发了BottomDialog")
    }
}