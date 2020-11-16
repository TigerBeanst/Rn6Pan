package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonParser
import com.jakting.rn6pan.MainActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.api.data.FileOrDirectory
import com.jakting.rn6pan.user.FileListActivity
import com.jakting.rn6pan.utils.*
import com.jakting.rn6pan.utils.MyApplication.Companion.appContext
import com.jakting.rn6pan.utils.MyApplication.Companion.nowPath
import com.jakting.rn6pan.utils.MyApplication.Companion.userInfo
import com.maning.imagebrowserlibrary.ImageEngine
import com.maning.imagebrowserlibrary.MNImageBrowser
import com.maning.imagebrowserlibrary.model.ImageBrowserConfig.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_file_list.*
import kotlinx.android.synthetic.main.activity_user_file_list.view.*
import kotlinx.android.synthetic.main.content_file_info.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat


class FileListAdapter(val fileOrDirectoryList: List<FileOrDirectory>, val isRootPath: Boolean,val activity:FileListActivity) :
    RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    companion object {
        lateinit var parentContext: Context
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileOrDirectoryIcon: ImageView = view.findViewById(R.id.file_list_fileicon)
        val fileOrDirectoryName: TextView = view.findViewById(R.id.file_list_filename)
        val fileOrDirectoryInfo: TextView = view.findViewById(R.id.file_list_info)
        val fileOrDirectoryImagePreview: ImageView = view.findViewById(R.id.file_list_image_preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        parentContext = parent.context
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_file_or_directory, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val fileOrDirectory = fileOrDirectoryList[position]
            /*
                ===============文件夹==============
             */
            if (fileOrDirectory.directory) {
                nowPath = fileOrDirectory.path
                activity.file_list_swipeLayout.autoRefresh()

            } else {
                when {
                    fileOrDirectory.mime.contains("video") /*视频*/ -> {
                        if (userInfo.vip != 0) { //已订阅

                        } else { //未订阅
                            MaterialAlertDialogBuilder(parent.context)
                                .setTitle(parent.context.getString(R.string.file_video_not_vip))
                                .setMessage(parent.context.getString(R.string.file_video_not_vip_desc))
                                .setPositiveButton(parent.context.getString(R.string.ok)) { _, _ -> }
                                .show()
                        }
                    }
                    fileOrDirectory.mime.contains("image") /*图片*/ -> {
                        getImagePreviewURL(fileOrDirectory.identity, viewHolder)
                    }
                    fileOrDirectory.mime.contains("application") /*可执行文件*/ -> {

                    }
                }
            }
            viewHolder.itemView.setOnLongClickListener {
                val position = viewHolder.adapterPosition
                val fileOrDirectory = fileOrDirectoryList[position]
                showBottomDialog(fileOrDirectory, parent)
                true
            }
        }
        return viewHolder
    }

    private fun getImagePreviewURL(
        identity: String,
        viewHolder: ViewHolder
    ) {
        val jsonForPost = "{\"identity\":\"$identity\"}"
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"), jsonForPost
            )
        val observable =
            EncapsulateRetrofit.init().getImagePreview(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ picturePreview ->
                logd("onNext // getImagePreview")
                clickImage(picturePreview.playAddress, viewHolder)
            }) { t ->
                logd("onError // getImagePreview")
                val errorString: String = getErrorString(t)
                logd(errorString)
                val errorJsonObject = JsonParser().parse(errorString).asJsonObject
                if (!errorJsonObject.get("success").asBoolean) {
                    MaterialAlertDialogBuilder(parentContext)
                        .setTitle(parentContext.getString(R.string.file_image_not_preview))
                        .setMessage(parentContext.getString(R.string.file_image_not_preview_desc))
                        .setPositiveButton(parentContext.getString(R.string.ok)) { _, _ -> }
                        .show()
                }
            }
    }

    private fun clickImage(playAdress: String, viewHolder: ViewHolder) {
        val transformType = TransformType.Transform_Default
        val indicatorType = IndicatorType.Indicator_Number
        val screenOrientationType = ScreenOrientationType.Screenorientation_Default
        val imageEngine: ImageEngine = GlideImageEngine()
        MNImageBrowser.with(parentContext)
            .setTransformType(transformType) //页面切换效果
            .setIndicatorType(indicatorType) //指示器效果
            .setIndicatorHide(false) //设置隐藏指示器
            .setCurrentPosition(1) //当前位置
            .setImageEngine(imageEngine) //图片引擎
            .setImageUrl(playAdress) //图片集合
            .setScreenOrientationType(screenOrientationType) //方向设置
            .setOnClickListener { activity, view, position, url ->

            }
            .setOnLongClickListener { activity, view, position, url ->

            }
            .setFullScreenMode(false) //打开动画
            .show(viewHolder.fileOrDirectoryImagePreview)
    }


    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileOrDirectory = fileOrDirectoryList[position]
        if (fileOrDirectory.directory) {
            holder.fileOrDirectoryIcon.setImageDrawable(
                ContextCompat.getDrawable(appContext, R.drawable.file_icon_directory)
            )
        } else {
            when {
                fileOrDirectory.mime.contains("video") -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_video)
                    )
                }
                fileOrDirectory.mime.contains("application/zip") -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_zip)
                    )
                }
                fileOrDirectory.mime.contains("application/x-dosexec") -> {
                    holder.fileOrDirectoryIcon.setImageDrawable(
                        ContextCompat.getDrawable(appContext, R.drawable.file_icon_exe)
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

//    private fun showDialogAskPreview() {
//        val items = arrayOf(
//            getString(R.string.file_sort_default),
//            getString(R.string.file_sort_filename_asc)
//        )
//        MaterialAlertDialogBuilder(parentContext)
//            .setTitle(resources.getString(R.string.file_sort_title))
//            .setItems(items) { dialog, which ->
//                when (which) {
//                    0 -> { //恢复默认排序
//                        defaultOrder = true
//                        file_list_swipeLayout.autoRefresh()
//                    }
//                    1 -> { //按文件名正序
//                        nameOrderBy = "asc"
//                        orderFlag = 0
//                        defaultOrder = false
//                        file_list_swipeLayout.autoRefresh()
//                    }
//                    2 -> { //按文件名倒序
//                        nameOrderBy = "desc"
//                        orderFlag = 0
//                        defaultOrder = false
//                        file_list_swipeLayout.autoRefresh()
//                    }
//                    3 -> { //按创建时间正序
//                        ctimeOrderBy = "asc"
//                        orderFlag = 1
//                        defaultOrder = false
//                        file_list_swipeLayout.autoRefresh()
//                    }
//                    4 -> { //按创建时间倒序
//                        ctimeOrderBy = "desc"
//                        orderFlag = 1
//                        defaultOrder = false
//                        file_list_swipeLayout.autoRefresh()
//                    }
//                }
//            }
//            .show()
//    }

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