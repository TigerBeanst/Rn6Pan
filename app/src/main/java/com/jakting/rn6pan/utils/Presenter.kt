package com.jakting.rn6pan.utils

import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.databinding.ObservableBoolean
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonParser
import com.jakting.rn6pan.R
import com.jakting.rn6pan.adapter.FileListAdapter
import com.jakting.rn6pan.api.data.FileOrDirectory
import com.jakting.rn6pan.user.FileListActivity
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

class Presenter(var context: Context) : ActionMode.Callback, ItemListener {
    private var mActionMode: ActionMode? = null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        val menuInflater = MenuInflater(context)
        menuInflater.inflate(R.menu.menu_file_list_multi, menu)
//        (context as FileListActivity).adapter.startActionMode()
        mActionMode = mode
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.delete -> mAdapter.deleteItems()
//            R.id.select_all -> mAdapter.selectAll()
            else -> {
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        (context as FileListActivity).adapter.stopActionMode()
        mActionMode = null
    }

    override fun onClick(v: View) {
        val parentContext = v.context as FileListActivity
        val position = v.getTag(R.id.item_position) as Int
        val fileOrDirectory = v.getTag(R.id.item_fileOrDirectory) as FileOrDirectory
        val viewHolder = v.getTag(R.id.item_viewholder) as FileListAdapter.ViewHolder
        if (mActionMode == null) {
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
                    fileOrDirectory.mime.contains("video") /*视频*/ -> {
                        if (MyApplication.userInfo.vip != 0) { //已订阅
                            val intent = Intent(parentContext, PlayerActivity::class.java)
                            intent.putExtra("identity", fileOrDirectory.identity)
                            parentContext.startActivity(intent)
                        } else { //未订阅
                            MaterialAlertDialogBuilder(parentContext)
                                .setTitle(parentContext.getString(R.string.file_video_not_vip))
                                .setMessage(parentContext.getString(R.string.file_video_not_vip_desc))
                                .setPositiveButton(parentContext.getString(R.string.ok)) { _, _ -> }
                                .show()
                        }
                    }
                    fileOrDirectory.mime.contains("image") /*图片*/ -> {
                        getImagePreviewURL(fileOrDirectory.identity, viewHolder, parentContext)
                    }
                    fileOrDirectory.mime.contains("application") /*可执行文件*/ -> {

                    }
                }
            }
        } else {
            //长按了
//            (context as FileListActivity).adapter.mBooleanList[position] = ObservableBoolean(true)

            val obTrue = ObservableBoolean(true)
            val obFalse = ObservableBoolean(false)
            viewHolder.mBinding.checked =
                if (viewHolder.mBinding.checked == obTrue) obFalse else obTrue
//            (context as FileListActivity).adapter.mBooleanList[position] = viewHolder.mBinding.checked as ObservableBoolean
        }

    }

    override fun onLongClick(v: View): Boolean {
        (context as FileListActivity).adapter.mSwitch.set(true)
        val parentContext = v.context as FileListActivity
        val position = v.getTag(R.id.item_position) as Int
        val fileOrDirectory = v.getTag(R.id.item_fileOrDirectory) as FileOrDirectory
        val viewHolder = v.getTag(R.id.item_viewholder) as FileListAdapter.ViewHolder
        if (mActionMode != null) {
            return false;
        }
        mActionMode = (context as FileListActivity).startSupportActionMode(this)
        viewHolder.mBinding.checked = ObservableBoolean(true)
        return true
    }

    private fun getImagePreviewURL(
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
            EncapsulateRetrofit.init().getImagePreview(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ picturePreview ->
                logd("onNext // getImagePreview")
                clickImage(picturePreview.playAddress, viewHolder, parentContext)
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