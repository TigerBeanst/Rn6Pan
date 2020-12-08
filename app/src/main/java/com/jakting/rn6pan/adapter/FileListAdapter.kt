package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.user.FileListActivity
import com.jakting.rn6pan.api.data.FileOrDirectory
import com.jakting.rn6pan.databinding.ItemFileOrDirectoryBinding
import com.jakting.rn6pan.utils.*
import com.jakting.rn6pan.utils.MyApplication.Companion.appContext
import kotlinx.android.synthetic.main.content_file_info.view.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


class FileListAdapter(
    private val fileOrDirectoryList: List<FileOrDirectory>,
    private val activity: FileListActivity
) :
    RecyclerView.Adapter<FileListAdapter.ViewHolder>() {
    lateinit var parentContext: Context
    lateinit var mListener: ItemListener
    var mSwitch: ObservableBoolean = ObservableBoolean(false)
    var mBooleanList: ArrayList<ObservableBoolean> = ArrayList()

    init {
        for (i in fileOrDirectoryList.indices) {
            mBooleanList.add(ObservableBoolean(false))
        }
        logd("============================================")
        logd("fileOrDirectoryList indices长度为：" + fileOrDirectoryList.indices)
        logd("fileOrDirectoryList size长度为：" + fileOrDirectoryList.size)
        logd("mBooleanList长度为：" + mBooleanList.size)
    }


    fun setListener(listener: ItemListener) {
        mListener = listener
    }

    fun startActionMode() {
        mSwitch.set(true)
    }

    fun stopActionMode() {
        mSwitch.set(false)
        mBooleanList.fill(ObservableBoolean(false))
        this.notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),View.OnCreateContextMenuListener {
        val fileOrDirectoryIcon: ImageView = view.findViewById(R.id.file_list_fileicon)
        val fileOrDirectoryName: TextView = view.findViewById(R.id.file_list_filename)
        val fileOrDirectoryInfo: TextView = view.findViewById(R.id.file_list_info)
        val fileOrDirectoryImagePreview: ImageView = view.findViewById(R.id.file_list_image_preview)
        val fileOrDirectoryMoreButton: ImageButton = view.findViewById(R.id.file_list_more)
        var mBinding: ItemFileOrDirectoryBinding = DataBindingUtil.bind(view)!!

        init {
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenu.ContextMenuInfo
        ) {
            menu.setHeaderTitle("subscirbe title");
            menu.add(ContextMenu.NONE, 0, ContextMenu.NONE, "添加");
            menu.add(ContextMenu.NONE, 1, ContextMenu.NONE, "删除");
            menu.add(ContextMenu.NONE, 2, ContextMenu.NONE, "修改");
        }
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
        val fileOrDirectory = fileOrDirectoryList[position]
        holder.itemView.setTag(R.id.item_position, position)
        holder.itemView.setTag(R.id.item_fileOrDirectory, fileOrDirectory)
        holder.itemView.setTag(R.id.item_viewholder, holder)
        holder.mBinding.visible = mSwitch
        holder.mBinding.checked = mBooleanList[position]
        holder.mBinding.listener = mListener
        holder.fileOrDirectoryMoreButton.setOnClickListener {
            val popup = PopupMenu(parentContext, holder.fileOrDirectoryMoreButton)
            popup.inflate(R.menu.menu_file_item_more)
            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menu_file_more_download->{
                        toast("test")
                    }
                    R.id.menu_file_more_copy->{

                    }
                    R.id.menu_file_more_move->{

                    }
                }
                true
            }
            popup.show()
        }
        if (fileOrDirectory.directory) {
            holder.fileOrDirectoryIcon.setImageDrawable(
                ContextCompat.getDrawable(appContext, R.drawable.file_icon_directory)
            )
        } else {
            when {
                isExtVideo(fileOrDirectory.ext) -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_video)
                    )
                }
                isExtImage(fileOrDirectory.ext) -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_pic)
                    )
                }
                isExtCompressed(fileOrDirectory.ext) -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_zip)
                    )
                }
                isExtAndroid(fileOrDirectory.ext) -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_android)
                    )
                }
                fileOrDirectory.mime.contains("application/x-dosexec") -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_exe)
                    )
                }
                fileOrDirectory.mime.contains("text") -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_txt)
                    )
                }
                else -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_unknown)
                    )
                }
            }
        }
        holder.fileOrDirectoryName.hint = fileOrDirectory.name
        holder.fileOrDirectoryInfo.hint =
            getPrintSize(fileOrDirectory.size) + " " + SimpleDateFormat("YYYY/MM/dd").format(
                fileOrDirectory.ctime
            )
    }

    override fun getItemCount() = fileOrDirectoryList.size

    private fun showBottomDialog(fileOrDirectory: FileOrDirectory, parent: ViewGroup) {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.content_file_info, null)
        view.file_one_filename.text = fileOrDirectory.name
        view.file_one_size.text = getPrintSize(fileOrDirectory.size)
        val dialog = BottomSheetDialog(view.context)
        dialog.setContentView(view.file_one_dialog)
        dialog.show()
        logd("触发了BottomDialog")
    }
}