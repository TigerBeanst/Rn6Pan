package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.user.FileListActivity
import com.jakting.rn6pan.api.accessAPI
import com.jakting.rn6pan.api.data.FileLabelItem
import com.jakting.rn6pan.api.data.FileOrDirectory
import com.jakting.rn6pan.databinding.ItemFileOrDirectoryBinding
import com.jakting.rn6pan.utils.*
import com.jakting.rn6pan.utils.MyApplication.Companion.appContext
import com.jakting.rn6pan.utils.database.DownloadListTable
import com.jakting.rn6pan.utils.download.getNameFromUrl
import kotlinx.android.synthetic.main.activity_user_file_list.*
import org.litepal.LitePal
import org.litepal.extension.find
import java.text.SimpleDateFormat


class FileListAdapter(
    private val fileOrDirectoryList: List<FileOrDirectory>,
    private val activity: FileListActivity
) :
    RecyclerView.Adapter<FileListAdapter.ViewHolder>() {
    lateinit var parentContext: Context
    lateinit var dialogForStart: AlertDialog
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

    /**
     * 为数据绑定添加监听器
     * @param listener ItemListener
     */
    fun setListener(listener: ItemListener) {
        mListener = listener
    }

    /**
     * 启动 ActionMode
     */
    fun startActionMode() {
        mSwitch.set(true)
    }

    /**
     * 停止 ActionMode
     */
    fun stopActionMode() {
        mSwitch.set(false)
        mBooleanList.fill(ObservableBoolean(false))
        this.notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {
        val fileOrDirectoryLabel: LinearLayout = view.findViewById(R.id.file_list_filelabel)
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
                //点击更多
                when (it.itemId) {
                    R.id.menu_file_more_download -> {
                        //判断此任务是否已经下载过
                        val downloadListTable = LitePal.where(
                            "fileIdentity = ?",
                            fileOrDirectory.identity
                        )
                            .find<DownloadListTable>()
                        if (downloadListTable.isNotEmpty()) {
                            //此文件已创建过本地下载任务
                            logd("此文件已创建过本地下载任务")
                            //获取此下载任务实体
                            val downloadEntity: DownloadEntity =
                                Aria.download(parentContext)
                                    .getDownloadEntity(downloadListTable[0].fileTaskId)
                            //创建对话框对不同情况进行提示
                            val dialog = MaterialAlertDialogBuilder(parentContext)
                            dialog.apply {
                                when (downloadEntity.state) {
                                    1 -> { //完成
                                        setTitle(parentContext.getString(R.string.transmission_download_dialog_finish_title))
                                        setMessage(parentContext.getString(R.string.transmission_download_dialog_finish_msg))
                                        setNegativeButton(parentContext.getString(R.string.cancel)) { _, _ ->
                                        }
                                        setPositiveButton(parentContext.getString(R.string.ok)) { _, _ ->
                                        }
                                    }
                                    2 -> { //停止
                                        setTitle(parentContext.getString(R.string.transmission_download_dialog_resume_title))
                                        setMessage(parentContext.getString(R.string.transmission_download_dialog_resume_msg))
                                        setNegativeButton(parentContext.getString(R.string.cancel)) { _, _ ->
                                        }
                                        setPositiveButton(parentContext.getString(R.string.ok)) { _, _ ->
                                        }
                                    }
                                    4 -> { //正在下载
                                        toast(parentContext.getString(R.string.transmission_download_dialog_doing))
                                    }
                                }
                                show()
                            }
                        } else {
                            //此文件未创建过本地下载任务
                            getDownloadAddress(fileOrDirectory.identity)
                        }
                    }
                    R.id.menu_file_more_download_to -> {
                        toast("test")
                    }
                    R.id.menu_file_more_copy -> {

                    }
                    R.id.menu_file_more_move -> {

                    }
                    R.id.menu_file_more_rename -> {
                        clickMoreMenuRename(fileOrDirectory, holder.itemView)
                    }
                    R.id.menu_file_more_star -> {
                        clickMoreMenuStar(fileOrDirectory)
                    }
                    R.id.menu_file_more_delete -> {

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
        if (fileOrDirectory.label != 0) {
            holder.fileOrDirectoryLabel.setBackgroundColor((0xFF000000 or fileOrDirectory.label.toLong()).toInt())
        } else {
            holder.fileOrDirectoryLabel.setBackgroundColor(Color.TRANSPARENT)
        }
        holder.fileOrDirectoryName.apply {
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint = fileOrDirectory.name
        }
        val params = holder.fileOrDirectoryInfo.layoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.fileOrDirectoryInfo.apply {
            layoutParams = params
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint =
                getPrintSize(fileOrDirectory.size) + "   " + SimpleDateFormat("YYYY/MM/dd").format(
                    fileOrDirectory.ctime
                )
        }
    }

    override fun getItemCount() = fileOrDirectoryList.size

    /**
     * 点击星标
     * @param fileOrDirectory FileOrDirectory
     */
    private fun clickMoreMenuStar(fileOrDirectory: FileOrDirectory) {
        val arrayOfLabelList = mutableListOf<FileLabelItem>()
        for (fileLabel in (parentContext as FileListActivity).fileLabelList.dataList) {
            val bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(
                Color.parseColor(
                    java.lang.String.format(
                        "#%06X",
                        0xFFFFFF and fileLabel.identity
                    )
                )
            )
            val nowStar =
                if (fileOrDirectory.label == fileLabel.identity) parentContext.getString(R.string.file_more_star_now) else ""
            arrayOfLabelList.add(
                FileLabelItem(
                    fileLabel.name + nowStar,
                    BitmapDrawable(parentContext.resources, bitmap),
                    fileLabel.identity
                )
            )
        }
        val items = arrayOfLabelList.toTypedArray()

        val adapterDialog: ListAdapter = object : ArrayAdapter<FileLabelItem?>(
            parentContext,
            android.R.layout.select_dialog_item,
            android.R.id.text1,
            items
        ) {
            @SuppressLint("ViewHolder", "SetTextI18n")
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val v = super.getView(position, convertView, parent)
                //点击以设置星标
                v.setOnClickListener {
                    dialogForStart.dismiss()
                    addLabel(fileOrDirectory.identity, arrayOfLabelList[position].identity)
                }
                val tv = v.findViewById(android.R.id.text1) as TextView
                tv.text = "   " + arrayOfLabelList[position].text
                tv.textSize = 16F
                tv.setPadding(50, 30, 50, 30)
                //Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(
                    arrayOfLabelList[position].drawable,
                    null,
                    null,
                    null
                )
                return v
            }
        }

        dialogForStart = MaterialAlertDialogBuilder(parentContext)
            .setTitle(parentContext.getString(R.string.file_more_star_title))
            .setAdapter(adapterDialog) { dialog, item ->
            }
            .setNeutralButton(parentContext.getString(R.string.file_more_star_clear)) { _, _ ->
                removeLabel(fileOrDirectory.identity)
            }
            .show()
    }

    /**
     * 点击重命名
     * @param fileOrDirectory FileOrDirectory
     * @param itemView View
     */
    private fun clickMoreMenuRename(fileOrDirectory: FileOrDirectory, itemView: View) {
        val baseView = itemView.findViewById<View>(R.id.dialog_layout)
        val viewInflated: View = LayoutInflater.from(parentContext)
            .inflate(R.layout.dialog_edittext, baseView as ViewGroup?, false)
        val textInputLayout =
            viewInflated.findViewById(R.id.dialog_textField) as TextInputLayout
        val editInputLayout =
            viewInflated.findViewById(R.id.dialog_editText) as TextInputEditText
        textInputLayout.hint =
            if (fileOrDirectory.directory) parentContext.getString(R.string.file_more_rename_directoryname) else parentContext.getString(
                R.string.file_more_rename_filename
            )
        editInputLayout.isFocusable = true
        editInputLayout.requestFocus()
        editInputLayout.setText(fileOrDirectory.name)
        MaterialAlertDialogBuilder(parentContext)
            .setTitle(parentContext.getString(R.string.file_more_rename))
            .setView(viewInflated)
            .setNegativeButton(parentContext.getString(R.string.cancel)) { _, _ ->
                // Respond to negative button press
            }
            .setPositiveButton(parentContext.getString(R.string.ok)) { dialog, _ ->
                val editString = editInputLayout.text.toString()
                if (isStringIllegal(editString) || editString == fileOrDirectory.name) {
                    toast(parentContext.getString(R.string.file_more_rename_fail))
                } else {
                    renameFile(fileOrDirectory.identity, editString, fileOrDirectory.directory)
                }
            }
            .show()
    }

    /**
     * 添加星标
     * @param identity String
     * @param label Int
     */
    private fun addLabel(identity: String, label: Int) {
        val jsonForPost =
            "{\"sourceIdentity\":[\"$identity\"],\"label\":$label}"
        accessAPI(
            {
                addLabel(getPostBody(jsonForPost))
            }, {
                logd("onNext // addLabel")
                toast(parentContext.getString(R.string.file_more_star_set_success))
                (parentContext as FileListActivity).file_list_swipeLayout.autoRefresh()
            }) {
            logd("onError // addLabel")
            toast(parentContext.getString(R.string.action_fail))
        }
    }

    /**
     * 移除星标
     * @param identity String
     */
    private fun removeLabel(identity: String) {
        val jsonForPost =
            "{\"sourceIdentity\":[\"$identity\"]}"
        accessAPI(
            {
                removeLabel(getPostBody(jsonForPost))
            }, {
                logd("onNext // removeLabel")
                toast(parentContext.getString(R.string.file_more_star_remove_success))
                (parentContext as FileListActivity).file_list_swipeLayout.autoRefresh()
            }) {
            logd("onError // removeLabel")
            toast(parentContext.getString(R.string.action_fail))
        }
    }

    /**
     * 重命名文件
     * @param identity String
     * @param name String
     * @param isDirectory Boolean
     */
    private fun renameFile(identity: String, name: String, isDirectory: Boolean) {
        val jsonForPost =
            "{\"identity\":\"$identity\",\"name\":\"$name\"}"
        accessAPI(
            {
                renameFile(getPostBody(jsonForPost))
            }, {
                logd("onNext // renameFile")
                if (isDirectory) {
                    toast(parentContext.getString(R.string.file_more_rename_success_directory))
                } else {
                    toast(parentContext.getString(R.string.file_more_rename_success_file))
                }
                (parentContext as FileListActivity).file_list_swipeLayout.autoRefresh()
            }) {
            logd("onError // renameFile")
            toast(parentContext.getString(R.string.action_fail))
        }
    }

    /**
     * 移动文件（没写完）
     * @param identity String
     * @param name String
     */
    private fun moveFile(identity: String, name: String) {
        val jsonForPost =
            "{\"sourceIdentity\":[\"$identity\"]}"
        accessAPI(
            {
                removeLabel(getPostBody(jsonForPost))
            }, {
                logd("onNext // removeLabel")
                toast(parentContext.getString(R.string.file_more_star_remove_success))
                (parentContext as FileListActivity).file_list_swipeLayout.autoRefresh()
            }) {
            logd("onError // removeLabel")
            toast(parentContext.getString(R.string.action_fail))
        }
    }

    /**
     * 复制文件（没写完）
     * @param identity String
     * @param name String
     */
    private fun copyFile(identity: String, name: String) {
        val jsonForPost =
            "{\"sourceIdentity\":[\"$identity\"]}"

        accessAPI(
            {
                removeLabel(getPostBody(jsonForPost))
            }, {
                logd("onNext // removeLabel")
                toast(parentContext.getString(R.string.file_more_star_remove_success))
                (parentContext as FileListActivity).file_list_swipeLayout.autoRefresh()
            }) {
            logd("onError // removeLabel")
            toast(parentContext.getString(R.string.action_fail))
        }
    }

    /**
     * 获取下载地址
     * @param videoIdentity String
     */
    private fun getDownloadAddress(videoIdentity: String) {
        val jsonForPost = "{\"identity\":\"$videoIdentity\"}"
        accessAPI(
            {
                getDownloadAddress(getPostBody(jsonForPost))
            }, { objectReturn ->
                val fileOrDirectory = objectReturn as FileOrDirectory
                logd("onNext // getDownloadAddress")
                val taskId = Aria.download(parentContext)
                    .load(fileOrDirectory.downloadAddress)
                    .ignoreCheckPermissions()
                    .setExtendField(Gson().toJson(fileOrDirectory))
                    .setFilePath(
                        parentContext.getExternalFilesDir("downloads")
                            .toString() + "/" + getNameFromUrl(
                            fileOrDirectory.downloadAddress
                        )
                    )    //文件保存路径
                    .create()
                if (taskId != (-1).toLong()) {
                    val downloadListTable = DownloadListTable()
                    downloadListTable.fileTaskId = taskId
                    downloadListTable.fileIdentity = fileOrDirectory.identity
                    downloadListTable.fileName = fileOrDirectory.name
                    downloadListTable.filePath = fileOrDirectory.path
                    downloadListTable.save()
                }
            }) { t ->
            logd("onError // getDownloadAddress")
            val errorString: String = getErrorString(t)
            logd(errorString)
            toast(parentContext.getString(R.string.action_fail))
        }
    }

}