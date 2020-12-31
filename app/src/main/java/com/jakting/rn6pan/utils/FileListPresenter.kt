package com.jakting.rn6pan.utils

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableBoolean
import com.afollestad.materialcab.attached.isDestroyed
import com.afollestad.materialcab.createCab
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonParser
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.player.PlayerActivity
import com.jakting.rn6pan.adapter.FileListAdapter
import com.jakting.rn6pan.api.data.FileOrDirectory
import com.jakting.rn6pan.activity.user.FileListActivity
import com.maning.imagebrowserlibrary.ImageEngine
import com.maning.imagebrowserlibrary.MNImageBrowser
import com.maning.imagebrowserlibrary.model.ImageBrowserConfig
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_file_list.*
import okhttp3.MediaType
import okhttp3.RequestBody

interface ItemListener {
    fun onClick(v: View)
    fun onLongClick(v: View): Boolean
}

class Presenter(var context: Context) : ItemListener {
    private var mActionMode: ActionMode? = null
    private val obTrue = ObservableBoolean(true)
    private val obFalse = ObservableBoolean(false)

    override fun onClick(v: View) {
        val parentContext = v.context as FileListActivity
        val position = v.getTag(R.id.item_position) as Int
        val fileOrDirectory = v.getTag(R.id.item_fileOrDirectory) as FileOrDirectory
        val viewHolder = v.getTag(R.id.item_viewholder) as FileListAdapter.ViewHolder
        if (parentContext.cab.isDestroyed()) {
            //没有长按
            if (parentContext.isShowFabMenu) parentContext.hideMenu()
            /*
                ===============文件夹==============
             */
            if (fileOrDirectory.directory) {
                MyApplication.parentPathList.add(fileOrDirectory.path)
                parentContext.file_list_swipeLayout.autoRefresh()

            } else {
                when {
                    isExtVideo(fileOrDirectory.ext) -> {
                        val intent = Intent(parentContext, PlayerActivity::class.java)
                        intent.putExtra("identity", fileOrDirectory.identity)
                        if (fileOrDirectory.size < 104857600 || System.currentTimeMillis() / 1000 - MyApplication.nowTimeStamp >= 5) {
                            parentContext.startActivity(intent)
                        } else {
                            toast(parentContext.getString(R.string.preview_loading))
                        }

                    }
                    isExtImage(fileOrDirectory.ext) -> {
                        getImagePreviewURLByDownloadAddress(
                            fileOrDirectory.identity,
                            viewHolder,
                            parentContext
                        )
                    }
                    else -> {
                        MaterialAlertDialogBuilder(parentContext)
                            .setTitle(parentContext.getString(R.string.file_file_not_preview))
                            .setMessage(
                                String.format(
                                    parentContext.getString(R.string.file_file_not_preview_desc),
                                    fileOrDirectory.mime
                                )
                            )
                            .setPositiveButton(parentContext.getString(R.string.ok)) { _, _ -> }
                            .show()
                    }
                }
            }
        } else {
            //长按了
//            (context as FileListActivity).adapter.mBooleanList[position] = ObservableBoolean(true)
            logd("当前这条是：${viewHolder.mBinding.checked?.get()}，文件名 ${viewHolder.fileOrDirectoryName}")
            viewHolder.mBinding.checked =
                if (viewHolder.mBinding.checked?.get() == true) obFalse else obTrue
            logd("点了之后是：${viewHolder.mBinding.checked?.get()}，文件名 ${viewHolder.fileOrDirectoryName}")
//            (context as FileListActivity).adapter.mBooleanList[position] = viewHolder.mBinding.checked as ObservableBoolean
        }

    }

    override fun onLongClick(v: View): Boolean {
        if (mActionMode != null) {
            return false
        }
        (context as FileListActivity).adapter.startActionMode()
        val parentContext = v.context as FileListActivity
        val position = v.getTag(R.id.item_position) as Int
        val fileOrDirectory = v.getTag(R.id.item_fileOrDirectory) as FileOrDirectory
        val viewHolder = v.getTag(R.id.item_viewholder) as FileListAdapter.ViewHolder
        viewHolder.mBinding.checked = obTrue
//        mActionMode = (context as FileListActivity).startSupportActionMode(this)
        parentContext.cab = parentContext.createCab(R.id.cab_stub) {
            title(R.string.app_name)
            menu(R.menu.menu_file_list_multi)
            backgroundColor(R.color.colorBackground)
            popupTheme(R.style.ThemeOverlay_AppCompat_DayNight_ActionBar)
            titleColor(R.color.colorFileFont)
            fadeIn()
            onCreate { cab, menu ->
                parentContext.supportActionBar?.hide()
            }
            onDestroy {
//                fadeIn()
                parentContext.supportActionBar?.show()
                (context as FileListActivity).adapter.stopActionMode()
                true
            }
        }
        viewHolder.mBinding.checked = ObservableBoolean(true)
        return true
    }


    private fun getImagePreviewURLByDownloadAddress(
        identity: String,
        viewHolder: FileListAdapter.ViewHolder,
        parentContext: Context
    ) {
        val jsonForPost = "{\"identity\":\"$identity\"}"
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"), jsonForPost
            )
        val observable =
            EncapsulateRetrofit.init().getDownloadAddress(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ picturePreview ->
                logd("onNext // getImagePreview")
                clickImage(picturePreview.downloadAddress, viewHolder, parentContext)
            }) { t ->
                logd("onError // getImagePreview")
                val errorString: String = getErrorString(t)
                logd(errorString)
                toast(parentContext.getString(R.string.action_fail))
            }
    }


    private fun clickImage(
        playAddress: String,
        viewHolder: FileListAdapter.ViewHolder,
        parentContext: Context
    ) {
        val transformType = ImageBrowserConfig.TransformType.Transform_Default
        val indicatorType = ImageBrowserConfig.IndicatorType.Indicator_Number
        val screenOrientationType =
            ImageBrowserConfig.ScreenOrientationType.ScreenOrientation_Portrait
        val imageEngine: ImageEngine = GlideImageEngine()
        MNImageBrowser.with(parentContext)
            .setTransformType(transformType) //页面切换效果
            .setIndicatorType(indicatorType) //指示器效果
            .setIndicatorHide(false) //设置隐藏指示器
            .setCurrentPosition(1) //当前位置
            .setImageEngine(imageEngine) //图片引擎
            .setImageUrl(playAddress) //图片集合
            .setScreenOrientationType(screenOrientationType) //方向设置
            .setOnClickListener { activity, view, position, url ->

            }
            .setOnLongClickListener { activity, view, position, url ->

            }
            .setFullScreenMode(false) //打开动画
            .show(viewHolder.fileOrDirectoryImagePreview)
    }


}