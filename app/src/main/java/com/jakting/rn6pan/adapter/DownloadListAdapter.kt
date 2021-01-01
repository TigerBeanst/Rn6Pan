package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.AbsEntity
import com.arialyy.aria.core.download.DownloadEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.user.FileListActivity
import com.jakting.rn6pan.api.data.FileLabelItem
import com.jakting.rn6pan.api.data.FileOrDirectory
import com.jakting.rn6pan.databinding.ItemFileOrDirectoryBinding
import com.jakting.rn6pan.utils.*
import com.jakting.rn6pan.utils.MyApplication.Companion.appContext
import com.jakting.rn6pan.utils.download.getNameFromUrl
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_file_list.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat


class DownloadListAdapter(
    private val downloadList: List<DownloadEntity>,
    private val activity: FileListActivity
) :
    RecyclerView.Adapter<DownloadListAdapter.ViewHolder>() {
    lateinit var parentContext: Context
    lateinit var dialogForStart: AlertDialog


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {
        val fileOrDirectoryName: TextView = view.findViewById(R.id.file_list_filename)
        val fileOrDirectoryInfo: TextView = view.findViewById(R.id.file_list_info)
        val fileOrDirectoryMoreButton: ImageButton = view.findViewById(R.id.file_list_more)

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
                .inflate(R.layout.item_download, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileOrDirectory = downloadList[position]
        holder.itemView.setTag(R.id.item_position, position)
        holder.itemView.setTag(R.id.item_fileOrDirectory, fileOrDirectory)
        holder.itemView.setTag(R.id.item_viewholder, holder)
        holder.fileOrDirectoryMoreButton.setOnClickListener {
            val popup = PopupMenu(parentContext, holder.fileOrDirectoryMoreButton)
            popup.inflate(R.menu.menu_file_item_more)
            popup.setOnMenuItemClickListener {
                //点击更多
                when (it.itemId) {
                    R.id.menu_file_more_download -> {
                    }
                    R.id.menu_file_more_download_to -> {
                        toast("test")
                    }
                    R.id.menu_file_more_copy -> {

                    }
                    R.id.menu_file_more_move -> {

                    }
                    R.id.menu_file_more_rename -> {
                    }
                    R.id.menu_file_more_star -> {
                    }
                    R.id.menu_file_more_delete -> {

                    }
                }
                true
            }
            popup.show()
        }
        holder.fileOrDirectoryName.apply{
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint = downloadList[position].fileName
        }
        val params = holder.fileOrDirectoryInfo.layoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.fileOrDirectoryInfo.apply{
            layoutParams = params
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint = downloadList[position].percent.toString()
        }
    }



    override fun getItemCount() = downloadList.size

}