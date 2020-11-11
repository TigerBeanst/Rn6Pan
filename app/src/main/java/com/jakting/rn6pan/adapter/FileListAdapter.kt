package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakting.rn6pan.R
import com.jakting.rn6pan.api.data.FileOrDirectory
import com.jakting.rn6pan.utils.GlideImageEngine
import com.jakting.rn6pan.utils.MyApplication
import com.jakting.rn6pan.utils.MyApplication.Companion.appContext
import com.jakting.rn6pan.utils.MyApplication.Companion.userInfo
import com.jakting.rn6pan.utils.getPrintSize
import com.jakting.rn6pan.utils.logd
import com.maning.imagebrowserlibrary.ImageEngine
import com.maning.imagebrowserlibrary.MNImageBrowser
import com.maning.imagebrowserlibrary.listeners.OnActivityLifeListener
import com.maning.imagebrowserlibrary.model.ImageBrowserConfig.*
import kotlinx.android.synthetic.main.content_file_info.view.*
import java.text.SimpleDateFormat


class FileListAdapter(val fileOrDirectoryList: List<FileOrDirectory>, val isRootPath: Boolean) :
    RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileOrDirectoryIcon: ImageView = view.findViewById(R.id.file_list_fileicon)
        val fileOrDirectoryName: TextView = view.findViewById(R.id.file_list_filename)
        val fileOrDirectoryInfo: TextView = view.findViewById(R.id.file_list_info)
        val fileOrDirectoryImagePreview: ImageView = view.findViewById(R.id.file_list_image_preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_file_or_directory, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val fileOrDirectory = fileOrDirectoryList[position]
            if (fileOrDirectory.directory) {

            } else if (fileOrDirectory.mime.contains("video")) {
                //播放视频
                if (userInfo.vip != 0) { //已订阅

                } else { //未订阅
                    MaterialAlertDialogBuilder(parent.context)
                        .setTitle(parent.context.getString(R.string.file_video_notvip))
                        .setMessage(parent.context.getString(R.string.file_video_notvip_desc))
                        .setPositiveButton(parent.context.getString(R.string.ok)) { _, _ -> }
                        .show()
                }
            } else if (fileOrDirectory.mime.contains("image")) {
                //图片预览
                val transformType = TransformType.Transform_Default
                val indicatorType = IndicatorType.Indicator_Number
                val screenOrientationType = ScreenOrientationType.Screenorientation_Default
                val imageEngine: ImageEngine = GlideImageEngine()
                val sourceImageList = arrayListOf(
                    "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png",
                    "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png",
                    "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png",
                    "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png",
                    "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png"
                )
                MNImageBrowser.with(parent.context)
                    .setTransformType(transformType) //页面切换效果
                    .setIndicatorType(indicatorType) //指示器效果
                    .setIndicatorHide(false) //设置隐藏指示器
                    .setCurrentPosition(position) //当前位置
                    .setImageEngine(imageEngine) //图片引擎
                    .setImageList(sourceImageList) //图片集合
                    .setScreenOrientationType(screenOrientationType) //方向设置
                    .setOnClickListener { activity, view, position, url ->

                    }
                    .setOnLongClickListener { activity, view, position, url ->

                    }
//                    .setOnPageChangeListener { position->
//                        Log.i(TAG, "onPageSelected:$position")
//                        if (tv_number_indicator != null) {
//                            tv_number_indicator.setText((position + 1).toString() + "/" + MNImageBrowser.getImageList().size)
//                        }
//                    }
                    .setFullScreenMode(false) //打开动画
                    .show(viewHolder.fileOrDirectoryImagePreview)
//                val createDestinationPostBody =
//                    RequestBody.create(
//                        MediaType.parse("application/json"),
//                        "{\"identity\":\"${fileOrDirectory.identity}\"}"
//                    )
//                val observable = EncapsulateRetrofit.init()
//                    .getImagePreview(createDestinationPostBody)
//                observable.subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({ picturePreview ->
//                        logd("onNext // getImagePreview")
//                        val lists = listOf(PicturePreview(url="https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png"))
//                        StfalconImageViewer.Builder<PicturePreview>(
//                            appContext,
//                            picturePreview
//                        ) { view, image ->
//                            Glide.with(appContext)
//                                .load("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png")
//                                .into(view)
//                        }.show()
//                    }) { t ->
//                        logd("onError // getImagePreview")
//                        t.printStackTrace()
//                    }
            }
        }
        viewHolder.itemView.setOnLongClickListener {
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
                ContextCompat.getDrawable(appContext, R.drawable.file_icon_directory)
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
                    ContextCompat.getDrawable(appContext, R.drawable.file_icon_zip)
                )
            } else {
                holder.fileOrDirectoryIcon.setImageDrawable(
                    ContextCompat.getDrawable(appContext, R.drawable.file_icon_unknown)
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

    private fun initBottomDialog(fileOrDirectory: FileOrDirectory, parent: ViewGroup) {
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