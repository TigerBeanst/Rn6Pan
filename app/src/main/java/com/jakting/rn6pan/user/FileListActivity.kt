package com.jakting.rn6pan.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.adapter.FileListAdapter
import com.jakting.rn6pan.api.data.FileOrDirectoryList
import com.jakting.rn6pan.databinding.ActivityUserFileListBinding
import com.jakting.rn6pan.utils.*
import com.jakting.rn6pan.utils.MyApplication.Companion.ctimeOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.defaultOrder
import com.jakting.rn6pan.utils.MyApplication.Companion.nameOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.orderFlag
import com.jakting.rn6pan.utils.MyApplication.Companion.parentPathList
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_file_list.*
import kotlinx.android.synthetic.main.dialog_edittext.*
import kotlinx.android.synthetic.main.file_fab_create_folder_layout.*
import kotlinx.android.synthetic.main.file_fab_transmission_layout.*
import kotlinx.android.synthetic.main.file_fab_upload_layout.*
import okhttp3.MediaType
import okhttp3.RequestBody


class FileListActivity : BaseActivity() {
    var isShowFabMenu = false
    lateinit var adapter: FileListAdapter
    lateinit var mBinding: ActivityUserFileListBinding

    companion object {
        lateinit var nowFileOrDirectoryList: FileOrDirectoryList
        var isUpToParentPath = false
        lateinit var showAnimation: Animation
        lateinit var hideAnimation: Animation
        lateinit var showMenuAnimation: Animation
        lateinit var hideMenuAnimation: Animation
        lateinit var mPresenter: Presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_file_list)
        mPresenter = Presenter(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initFAB()
        initBottomBarNavIcon()
        //下拉刷新
        file_list_swipeLayout.apply {
            setEnableRefresh(true)
            setPrimaryColorsId(R.color.colorAccent, R.color.colorPrimary)
            autoRefresh()
        }
        file_list_swipeLayout.setOnRefreshListener {
            if (isUpToParentPath) {
                parentPathList.removeAt(parentPathList.size - 1)
                isUpToParentPath = false
            }
            initFileOrDirectoryList()
        }
    }

    private fun initFAB() {
        showAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_scale_up)
        hideAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_scale_down)
        showMenuAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_slide_in_from_left)
        hideMenuAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_slide_in_from_right)
        file_fab.setOnClickListener {
            if (!isShowFabMenu) {
                //还没显示菜单
                showMenu()
            } else {
                //显示菜单了
                hideMenu()
            }
        }
        file_fab_upload_button.setOnClickListener { }
        file_fab_create_folder_button.setOnClickListener {
            val viewInflated: View = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edittext, dialog_layout as ViewGroup?, false)
            val textInputLayout =
                viewInflated.findViewById(R.id.dialog_textField) as TextInputLayout
            val editInputLayout =
                viewInflated.findViewById(R.id.dialog_editText) as TextInputEditText
            textInputLayout.hint = getString(R.string.file_create_folder_dialog)
            editInputLayout.isFocusable = true
            editInputLayout.requestFocus()
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.file_create_folder))
                .setView(viewInflated)
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                    // Respond to negative button press
                }
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                    val editString = editInputLayout.text.toString()
                    if (isStringIllegal(editString)) {
                        toast(getString(R.string.file_create_folder_fail))
                    } else {
                        toast(getString(R.string.loading))
                        createDirectory(editInputLayout.text.toString())
                    }
                }
                .show()
        }
        file_fab_transmission_button.setOnClickListener { }
    }

    private fun showMenu() {
        file_fab.startAnimation(showMenuAnimation)
        file_fab.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_close_24
            )
        )
        file_fab_upload.startAnimation(showAnimation)
        file_fab_create_folder.startAnimation(showAnimation)
        file_fab_transmission.startAnimation(showAnimation)
        file_fab_upload.visibility = View.VISIBLE
        file_fab_create_folder.visibility = View.VISIBLE
        file_fab_transmission.visibility = View.VISIBLE
        isShowFabMenu = true
    }

    fun hideMenu() {
        file_fab.startAnimation(hideMenuAnimation)
        file_fab.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_add_24
            )
        )
        file_fab_upload.startAnimation(hideAnimation)
        file_fab_create_folder.startAnimation(hideAnimation)
        file_fab_transmission.startAnimation(hideAnimation)
        file_fab_upload.visibility = View.INVISIBLE
        file_fab_create_folder.visibility = View.INVISIBLE
        file_fab_transmission.visibility = View.INVISIBLE
        isShowFabMenu = false
    }

    private fun initFileOrDirectoryList() {
        val jsonForPost =
            "{\"parentPath\":\"${parentPathList[parentPathList.size - 1]}\",\"orderby\":[" +
                    (if (!defaultOrder) {
                        (if (orderFlag == 0)
                            (if (nameOrderBy != "") "[\"name\",\"$nameOrderBy\"]" else "")
                        else
                            (if (ctimeOrderBy != "") "[\"ctime\",\"$ctimeOrderBy\"]" else ""))
                    } else "") +
                    "]}"
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"), jsonForPost
            )
        val observable =
            EncapsulateRetrofit.init().getFileOrDirectoryList(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fileOrDirectoryList ->
                logd("onNext // getFileOrDirectoryList")
                nowFileOrDirectoryList = fileOrDirectoryList
                setAdapter()
            }) { t ->
                logd("onError // getFileOrDirectoryList")
                t.printStackTrace()
                toast(getString(R.string.action_fail))
            }
    }

    private fun createDirectory(newFolderName: String) {
        val jsonForPost =
            "{\"parent\":\"${nowFileOrDirectoryList.parent.identity}\"," +
                    "\"path\":\"$newFolderName\"}"
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"), jsonForPost
            )
        val observable =
            EncapsulateRetrofit.init().createDirectory(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ CreateDirectory ->
                logd("onNext // createDirectory")
                toast(getString(R.string.file_create_folder_success))
                file_list_swipeLayout.autoRefresh()
                hideMenu()
            }) { t ->
                logd("onError // createDirectory")
                t.printStackTrace()
                toast(getString(R.string.action_fail))
            }
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(this)
        mBinding.fileListRecyclerView.layoutManager = layoutManager
        adapter = FileListAdapter(nowFileOrDirectoryList.dataList, this)
        adapter.setListener(mPresenter)
        if (parentPathList.size > 1) {
            if (supportActionBar != null) {
                supportActionBar!!.title = nowFileOrDirectoryList.parent.name
            }
        } else {
            if (supportActionBar != null) {
                supportActionBar!!.title = getString(R.string.file_toolbar_title)
            }
        }
        mBinding.fileListRecyclerView.adapter = adapter
        file_list_swipeLayout.finishRefresh(0)

    }

    private fun initBottomBarNavIcon() {
        bottomAppBar.setOnMenuItemClickListener {
            if (isShowFabMenu) hideMenu()
            when (it.itemId) {
                R.id.menu_file_sort -> {
                    val items = arrayOf(
                        getString(R.string.file_sort_default),
                        getString(R.string.file_sort_filename_asc),
                        getString(R.string.file_sort_filename_desc),
                        getString(R.string.file_sort_ctime_asc),
                        getString(R.string.file_sort_ctime_desc),
                    )
                    MaterialAlertDialogBuilder(this)
                        .setTitle(resources.getString(R.string.file_sort_title))
                        .setItems(items) { dialog, which ->
                            when (which) {
                                0 -> { //恢复默认排序
                                    defaultOrder = true
                                    file_list_swipeLayout.autoRefresh()
                                }
                                1 -> { //按文件名正序
                                    nameOrderBy = "asc"
                                    orderFlag = 0
                                    defaultOrder = false
                                    file_list_swipeLayout.autoRefresh()
                                }
                                2 -> { //按文件名倒序
                                    nameOrderBy = "desc"
                                    orderFlag = 0
                                    defaultOrder = false
                                    file_list_swipeLayout.autoRefresh()
                                }
                                3 -> { //按创建时间正序
                                    ctimeOrderBy = "asc"
                                    orderFlag = 1
                                    defaultOrder = false
                                    file_list_swipeLayout.autoRefresh()
                                }
                                4 -> { //按创建时间倒序
                                    ctimeOrderBy = "desc"
                                    orderFlag = 1
                                    defaultOrder = false
                                    file_list_swipeLayout.autoRefresh()
                                }
                            }
                        }
                        .show()
                    true
                }
                else -> false
            }
        }
        bottomAppBar.setNavigationOnClickListener {
            if (isShowFabMenu) hideMenu()
            if (parentPathList.size == 1) {
                toast(getString(R.string.file_to_parent_folder_toast))
            } else {
                backToParentPath()
            }
        }
    }

    private fun backToParentPath() {
        isUpToParentPath = true
        file_list_swipeLayout.autoRefresh()
    }

    override fun onBackPressed() {
        if (parentPathList.size == 1) {
            super.onBackPressed()
        } else {
            backToParentPath()
        }
    }
}