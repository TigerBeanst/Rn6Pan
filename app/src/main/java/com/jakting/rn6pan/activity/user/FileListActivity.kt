package com.jakting.rn6pan.activity.user

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.argb
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.adapter.FileListAdapter
import com.jakting.rn6pan.adapter.TransmissionAdapter
import com.jakting.rn6pan.api.accessAPI
import com.jakting.rn6pan.api.data.FileLabel
import com.jakting.rn6pan.api.data.FileLabelItem
import com.jakting.rn6pan.api.data.FileLabelList
import com.jakting.rn6pan.api.data.FileOrDirectoryList
import com.jakting.rn6pan.databinding.ActivityUserFileListBinding
import com.jakting.rn6pan.utils.*
import com.jakting.rn6pan.utils.MyApplication.Companion.appContext
import com.jakting.rn6pan.utils.MyApplication.Companion.ctimeOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.defaultOrder
import com.jakting.rn6pan.utils.MyApplication.Companion.labelFilter
import com.jakting.rn6pan.utils.MyApplication.Companion.nameOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.orderFlag
import com.jakting.rn6pan.utils.MyApplication.Companion.parentPathList
import com.jakting.rn6pan.utils.MyApplication.Companion.settingSharedPreferences
import com.jakting.rn6pan.utils.MyApplication.Companion.settingSharedPreferencesEditor
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.effet.RippleEffect
import com.takusemba.spotlight.shape.Circle
import kotlinx.android.synthetic.main.activity_choose_path.*
import kotlinx.android.synthetic.main.activity_user_file_list.*
import kotlinx.android.synthetic.main.activity_user_file_list.toolbar
import kotlinx.android.synthetic.main.content_file_transmission.*
import kotlinx.android.synthetic.main.content_file_transmission.view.*
import kotlinx.android.synthetic.main.dialog_edittext.*
import kotlinx.android.synthetic.main.file_fab_create_folder_layout.*
import kotlinx.android.synthetic.main.file_fab_upload_layout.*
import kotlinx.android.synthetic.main.layout_target.*
import kotlinx.android.synthetic.main.layout_target.view.*
import org.litepal.LitePal


class FileListActivity : BaseActivity(), ColorPickerDialogListener {
    var isShowFabMenu = false
    var nowOnPage = 0
    var searchWords = ""
    lateinit var fileLabelList: FileLabelList
    lateinit var fileListAdapter: FileListAdapter
    lateinit var mBinding: ActivityUserFileListBinding
    lateinit var dialogForLabelsList: AlertDialog
    lateinit var colorButton: MaterialButton

    var colorInt = 0
    var cab: AttachedCab? = null

    companion object {
        lateinit var nowFileOrDirectoryList: FileOrDirectoryList
        var isUpToParentPath = false
        lateinit var showAnimation: Animation
        lateinit var hideAnimation: Animation
        lateinit var showMenuAnimation: Animation
        lateinit var hideMenuAnimation: Animation
        lateinit var mPresenter: Presenter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_file_list_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishAfterTransition()
            R.id.menu_file_main_star -> {
                clickMenuStar()
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_file_list)
        mPresenter = Presenter(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        LitePal.initialize(appContext)
        initButton()
        initBottomBarNavIcon()
        //下拉刷新
        file_list_swipeLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setEnableAutoLoadMore(true)
            setEnableScrollContentWhenLoaded(true)
            setPrimaryColorsId(R.color.colorAccent, R.color.colorPrimary)
            autoRefresh()
        }
        file_list_swipeLayout.setOnRefreshListener {
            if (isUpToParentPath) {
                parentPathList.removeAt(parentPathList.size - 1)
                isUpToParentPath = false
            }
            bottomAppBar.performShow()
            nowOnPage = 0
            initFileOrDirectoryList(false, arrayListOf(false, false))
            initStarLabels()
        }
        file_list_swipeLayout.setOnLoadMoreListener {
            nowOnPage++
            initFileOrDirectoryList(true, arrayListOf(false, false))
        }
    }

    /**
     * 初始化 FAB
     */
    private fun initButton() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
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
                        createDirectory(editInputLayout.text.toString())
                    }
                }
                .show()
        }
    }

    /**
     * 显示 FAB 菜单
     */
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
//        file_fab_transmission.startAnimation(showAnimation)
        file_fab_upload.visibility = View.VISIBLE
        file_fab_create_folder.visibility = View.VISIBLE
//        file_fab_transmission.visibility = View.VISIBLE
        isShowFabMenu = true
    }

    /**
     * 隐藏 FAB 菜单
     */
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
        file_fab_upload.visibility = View.INVISIBLE
        file_fab_create_folder.visibility = View.INVISIBLE
        isShowFabMenu = false
    }

    /**
     * 初始化文件列表
     * ArrayList<Boolean>  参数1：是否搜索    参数2：是否为全局搜索
     * @param isLoadMore Boolean
     * @param isSearch ArrayList<Boolean>
     */
    private fun initFileOrDirectoryList(isLoadMore: Boolean, isSearch: ArrayList<Boolean>) {
        val jsonForPost =
            "{" +
                    if (!isSearch[1]) {  //如果不是全局搜索，则加入此字段
                        "\"parentPath\":\"${parentPathList[parentPathList.size - 1]}\","
                    } else {
                        ""
                    } +
                    if (isSearch[0]) { //如果是搜索的话
                        "\"search\": true,\"name\": \"3dm\","
                    } else {
                        "\"skip\": ${nowOnPage * 20},\"limit\":20,"
                    } +
                    (if (labelFilter != 0) "\"label\":$labelFilter," else "") +
                    "\"orderby\":[" +
                    (if (!defaultOrder) {
                        (if (orderFlag == 0)
                            (if (nameOrderBy != "") "[\"name\",\"$nameOrderBy\"]" else "")
                        else
                            (if (ctimeOrderBy != "") "[\"ctime\",\"$ctimeOrderBy\"]" else ""))
                    } else "") +
                    "]}"
        accessAPI(
            {
                getFileOrDirectoryList(getPostBody(jsonForPost))
            }, { objectReturn ->
                val fileOrDirectoryList = objectReturn as FileOrDirectoryList
                logd("onNext // getFileOrDirectoryList")
                if (isLoadMore) {
                    nowFileOrDirectoryList.dataList += fileOrDirectoryList.dataList
                } else {
                    nowFileOrDirectoryList = fileOrDirectoryList
                }
                setFileListAdapter()
            }) {
            toast(getString(R.string.action_fail))
        }
    }

    /**
     * 初始化星标
     */
    private fun initStarLabels() {
        accessAPI(
            {
                getLabelsList()
            }, { objectReturn ->
                val fileLabelList = objectReturn as FileLabelList
                logd("onNext // getFileOrDirectoryList")
                this.fileLabelList = fileLabelList
            }) {
            toast(getString(R.string.action_fail))
        }
    }

    /**
     * 创建文件夹
     * @param newFolderName String
     */
    private fun createDirectory(newFolderName: String) {
        val jsonForPost =
            "{\"parent\":\"${nowFileOrDirectoryList.parent.identity}\"," +
                    "\"path\":\"$newFolderName\"}"
        accessAPI(
            {
                createDirectory(getPostBody(jsonForPost))
            }, {
                logd("onNext // createDirectory")
                toast(getString(R.string.file_create_folder_success))
                file_list_swipeLayout.autoRefresh()
                hideMenu()
            }) {
            toast(getString(R.string.action_fail))
        }
    }

    /**
     * 设置文件列表适配器
     */
    private fun setFileListAdapter() {
        val layoutManager = LinearLayoutManager(this)
        mBinding.fileListRecyclerView.layoutManager = layoutManager
        fileListAdapter = FileListAdapter(nowFileOrDirectoryList.dataList, this)
        fileListAdapter.setListener(mPresenter)
        if (parentPathList.size > 1) {
            if (supportActionBar != null) {
                supportActionBar!!.title = nowFileOrDirectoryList.parent.name
            }
        } else {
            if (supportActionBar != null) {
                supportActionBar!!.title = getString(R.string.file_toolbar_title)
            }
        }
        mBinding.fileListRecyclerView.adapter = fileListAdapter
        mBinding.fileListRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (file_fab.isShown) {
                        file_fab.hide()
                    }
                } else {
                    if (!file_fab.isShown) {
                        file_fab.show()
                    }
                }
            }
        })
        file_list_swipeLayout.finishRefresh(0)
        file_list_swipeLayout.finishLoadMore(0)
        toolbar.menu.findItem(R.id.menu_file_main_star).isEnabled = true
        labelFilter = 0
    }

    /**
     * 初始化底部导航菜单
     */
    private fun initBottomBarNavIcon() {
        bottomAppBar.setOnMenuItemClickListener {
            if (isShowFabMenu) hideMenu()
            when (it.itemId) {
                R.id.menu_file_transmission -> {
                    clickBottomBarTransmission()
                    true
                }
                R.id.menu_file_sort -> {
                    clickBottomBarSort()
                    true
                }
                R.id.menu_file_search -> {
                    clickBottomBarSearch()
                    true
                }
                else -> false
            }
        }
        bottomAppBar.setNavigationOnClickListener {
            super.onBackPressed()
        }
    }

    /**
     * 回退到上一个文件夹
     */
    private fun backToParentPath() {
        isUpToParentPath = true
        file_list_swipeLayout.autoRefresh()
    }

    /**
     * 初始化向导
     */
    private fun initSpotlight() {
        val targets = ArrayList<Target>()
        val targetLayout = layoutInflater.inflate(R.layout.layout_target, FrameLayout(this))
        targetLayout.isClickable = true
        val backToParentTarget = Target.Builder()
            .setAnchor(toolbar.getToolBarItemView(toolbar.navigationIcon)!!)
            .setShape(Circle(100f))
            .setEffect(RippleEffect(100f, 200f, argb(255, 72, 166, 151)))
            .setOverlay(targetLayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    targetLayout.spotlight_title.text =
                        getString(R.string.spotlight_backToParentTarget_title)
                    targetLayout.spotlight_content.text =
                        getString(R.string.spotlight_backToParentTarget_content)
                }

                override fun onEnded() {}
            })
            .build()
        targets.add(backToParentTarget)

        val backToMainTarget = Target.Builder()
            .setAnchor(getBottomBarItemView(bottomAppBar.navigationIcon)!!)
            .setShape(Circle(100f))
            .setEffect(RippleEffect(100f, 200f, argb(255, 72, 166, 151)))
            .setOverlay(targetLayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    targetLayout.spotlight_title.text =
                        getString(R.string.spotlight_backToMainTarget_title)
                    targetLayout.spotlight_content.text =
                        getString(R.string.spotlight_backToMainTarget_content)
                }

                override fun onEnded() {}
            })
            .build()
        targets.add(backToMainTarget)

        val fabTarget = Target.Builder()
            .setAnchor(file_fab)
            .setShape(Circle(100f))
            .setEffect(RippleEffect(100f, 200f, argb(255, 72, 166, 151)))
            .setOverlay(targetLayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    targetLayout.spotlight_title.text =
                        getString(R.string.spotlight_fabTarget_title)
                    targetLayout.spotlight_content.text =
                        getString(R.string.spotlight_fabTarget_content)
                }

                override fun onEnded() {}
            })
            .build()
        targets.add(fabTarget)

        val tranTarget = Target.Builder()
            .setAnchor(findViewById<View>(R.id.menu_file_transmission))
            .setShape(Circle(100f))
            .setEffect(RippleEffect(100f, 200f, argb(255, 72, 166, 151)))
            .setOverlay(targetLayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    targetLayout.spotlight_title.text =
                        getString(R.string.spotlight_transmission_title)
                    targetLayout.spotlight_content.text =
                        getString(R.string.spotlight_transmission_content)
                }

                override fun onEnded() {}
            })
            .build()
        targets.add(tranTarget)

        val sortTarget = Target.Builder()
            .setAnchor(findViewById<View>(R.id.menu_file_sort))
            .setShape(Circle(100f))
            .setEffect(RippleEffect(100f, 200f, argb(255, 72, 166, 151)))
            .setOverlay(targetLayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    targetLayout.spotlight_title.text =
                        getString(R.string.spotlight_sortTarget_title)
                    targetLayout.spotlight_content.text =
                        getString(R.string.spotlight_sortTarget_content)
                    targetLayout.nextTargetButton.text = getString(R.string.spotlight_done)
                    (targetLayout.nextTargetButton as MaterialButton).icon =
                        ContextCompat.getDrawable(
                            this@FileListActivity,
                            R.drawable.ic_baseline_done_all_24
                        )
                    settingSharedPreferencesEditor.putBoolean("first_run", false)
                    settingSharedPreferencesEditor.apply()
                }

                override fun onEnded() {}
            })
            .build()
        targets.add(sortTarget)

        val spotlight = Spotlight.Builder(this)
            .setTargets(targets)
            .setBackgroundColorRes(R.color.spotlightBackground)
            .setDuration(1000L)
            .setAnimation(DecelerateInterpolator(2f))
            .setOnSpotlightListener(object : OnSpotlightListener {
                override fun onStarted() {}

                override fun onEnded() {}
            })
            .build()

        spotlight.start()
        targetLayout.nextTargetButton.setOnClickListener { spotlight.next() }
    }

    /**
     * 获取 BottomBar 图标的 View
     * @param drawable Drawable?
     * @return View?
     */
    private fun getBottomBarItemView(drawable: Drawable?): View? {
        val size: Int = bottomAppBar.childCount
//        logd("获取底部栏的详情：size为$size")
        for (i in 0 until size) {
            val child: View = bottomAppBar.getChildAt(i)
//            logd("获取底部栏的详情：view为$child")
            if (child is ImageButton) {
                if (child.drawable === drawable) {
//                    logd("获取底部栏的详情：drawable${child.drawable}")
                    return child
                }
            }
        }
        return null
    }

    /**
     * 点击 BottomBar 中的 传输列表
     */
    private fun clickBottomBarTransmission() {
        val view: View =
            LayoutInflater.from(this).inflate(R.layout.content_file_transmission, null)
        val bottomDialog = BottomSheetDialog(view.context)
        bottomDialog.setContentView(view)
        val mBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(view.parent as View)
        mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBehavior.peekHeight = 0;
        bottomDialog.show()
        logd("触发了BottomDialog")
        initAriaDownloader(view)
    }

    /**
     * 点击 BottomBar 中的 排序
     */
    private fun clickBottomBarSort() {
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
    }

    /**
     * 点击 BottomBar 中的 搜索
     */
    private fun clickBottomBarSearch() {
        val viewInflated: View = LayoutInflater.from(this@FileListActivity)
            .inflate(
                R.layout.dialog_edittext,
                dialog_layout as ViewGroup?,
                false
            )
        val textInputLayout =
            viewInflated.findViewById(R.id.dialog_textField) as TextInputLayout
        val editInputLayout =
            viewInflated.findViewById(R.id.dialog_editText) as TextInputEditText
        textInputLayout.hint =
            getString(R.string.file_search_dialog_title)
        editInputLayout.isFocusable = true
        editInputLayout.requestFocus()
        MaterialAlertDialogBuilder(this@FileListActivity)
            .setTitle(resources.getString(R.string.file_search_title))
            .setView(viewInflated)
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.file_search_dialog_all)) { _, _ ->
                val editString = editInputLayout.text.toString()
                if (editString.trim().isNotEmpty()) {
                    searchWords = editString
                    file_list_swipeLayout.finishLoadMoreWithNoMoreData()
                    initFileOrDirectoryList(false, arrayListOf(true, true))
                } else {
                    toast(getString(R.string.file_search_dialog_fail))
                }
            }
            .setNeutralButton(resources.getString(R.string.file_search_dialog_this)) { _, _ ->
                val editString = editInputLayout.text.toString()
                if (editString.trim().isNotEmpty()) {
                    searchWords = editString
                    file_list_swipeLayout.finishLoadMoreWithNoMoreData()
                    initFileOrDirectoryList(false, arrayListOf(true, false))
                } else {
                    toast(getString(R.string.file_search_dialog_fail))
                }
            }
            .show()
    }

    /**
     * 点击 Toolbar 中的 星标
     */
    private fun clickMenuStar() {
        val arrayOfLabelList = mutableListOf<FileLabelItem>()
        for (fileLabel in fileLabelList.dataList) {
//         BitmapDrawable(this.resources, bitmap)           val bitmap = Bitmap.createBitmap(30, 30,Bitmap.Config.ARGB_8888).eraseColor(Color.parseColor(fileLabel.identity))
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
                if (labelFilter == fileLabel.identity) getString(R.string.file_more_star_now) else ""
            arrayOfLabelList.add(
                FileLabelItem(
                    fileLabel.name + nowStar,
                    BitmapDrawable(this.resources, bitmap),
                    fileLabel.identity
                )
            )
        }
        val items = arrayOfLabelList.toTypedArray()

        val adapterDialog: ListAdapter = object : ArrayAdapter<FileLabelItem?>(
            this,
            android.R.layout.select_dialog_item,
            android.R.id.text1,
            items
        ) {
            @SuppressLint("ViewHolder")
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val v = super.getView(position, convertView, parent)
                v.setOnClickListener {
                    dialogForLabelsList.dismiss()
                    labelFilter = arrayOfLabelList[position].identity //已选中的星标的颜色值
                    file_list_swipeLayout.autoRefresh()
                }
                //长按列表中的条目以触发编辑星标操作
                v.setOnLongClickListener {
                    dialogForLabelsList.dismiss()
                    val viewInflated: View = LayoutInflater.from(this@FileListActivity)
                        .inflate(
                            R.layout.dialog_edittext,
                            dialog_layout as ViewGroup?,
                            false
                        )
                    val textInputLayout =
                        viewInflated.findViewById(R.id.dialog_textField) as TextInputLayout
                    val editInputLayout =
                        viewInflated.findViewById(R.id.dialog_editText) as TextInputEditText
                    textInputLayout.hint =
                        getString(R.string.file_toolbar_star_filter_modify_name)
                    editInputLayout.isFocusable = true
                    editInputLayout.requestFocus()
                    editInputLayout.setText(arrayOfLabelList[position].text)
                    MaterialAlertDialogBuilder(this@FileListActivity)
                        .setTitle(resources.getString(R.string.file_toolbar_star_filter_modify))
                        .setView(viewInflated)
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                            // Respond to negative button press
                            ColorPickerDialog.newBuilder().setColor(0xFF000000.toInt())
                                .show(this@FileListActivity)
                        }
                        .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                            val editString = editInputLayout.text.toString()
                            if (editString.trim().isNotEmpty()) {
                                modifyStarLabelName(
                                    arrayOfLabelList[position].identity,
                                    editString
                                )
                            } else {
                                toast(getString(R.string.file_toolbar_star_filter_modify_name_fail))
                            }
                        }
                        .setNeutralButton(resources.getString(R.string.file_toolbar_star_delete)) { _, _ ->
                            MaterialAlertDialogBuilder(this@FileListActivity)
                                .setMessage(resources.getString(R.string.file_toolbar_star_delete_msg))
                                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                                }
                                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                                    deleteLabel(arrayOfLabelList[position].identity)
                                }
                                .show()
                        }
                        .show()
                    true
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

        dialogForLabelsList = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.file_toolbar_star_filter))
            .setAdapter(adapterDialog) { dialog, item ->

            }
            //新增
            .setPositiveButton(
                R.string.file_toolbar_star_filter_add
            ) { _, _ ->
//                dialogForLabelsList.dismiss()
                val viewInflated: View = LayoutInflater.from(this@FileListActivity)
                    .inflate(
                        R.layout.dialog_edittext_with_color,
                        dialog_layout as ViewGroup?,
                        false
                    )
                val textInputLayout =
                    viewInflated.findViewById(R.id.dialog_textField) as TextInputLayout
                val editInputLayout =
                    viewInflated.findViewById(R.id.dialog_editText) as TextInputEditText
                colorButton =
                    viewInflated.findViewById(R.id.dialog_button_color) as MaterialButton
                colorButton.setOnClickListener {
                    ColorPickerDialog.newBuilder().setColor(0xFF000000.toInt())
                        .show(this@FileListActivity)
                }
                textInputLayout.hint =
                    getString(R.string.file_toolbar_star_add_name)
                MaterialAlertDialogBuilder(this@FileListActivity)
                    .setTitle(resources.getString(R.string.file_toolbar_star_add_title))
                    .setView(viewInflated)
                    .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                        val editString = editInputLayout.text.toString()
                        if (editString.trim().isEmpty()) {
                            toast(getString(R.string.file_create_folder_fail))
                        } else {
                            createStarLabel(editString)
                        }
                    }
                    .show()
            }
            //清除筛选
            .setNeutralButton(
                R.string.file_toolbar_star_filter_clear
            ) { _, _ ->
                labelFilter = 0
                file_list_swipeLayout.autoRefresh()
            }
            .show()
    }

    /**
     * 创建星标
     * @param name String
     */
    private fun createStarLabel(name: String) {
        val jsonForPost =
            "{\"name\":\"$name\",\"identity\":$colorInt}"
        accessAPI(
            {
                createLabel(getPostBody(jsonForPost))
            }, { objectReturn ->
                val fileLabel = objectReturn as FileLabel
                logd("onNext // createLabel")
                if (fileLabel.name != name) {
                    toast(getString(R.string.file_toolbar_star_add_fail))
                } else {
                    toast(getString(R.string.file_toolbar_star_add_success))
                    initStarLabels()
                }
            }) {
            toast(getString(R.string.file_toolbar_star_add_fail_server))
        }
    }

    /**
     * 修改星标名称
     * @param identity Int
     * @param name String
     */
    private fun modifyStarLabelName(identity: Int, name: String) {
        val jsonForPost =
            "{\"name\":\"${name}\"}"

        accessAPI(
            {
                modifyLabelName(getPostBody(jsonForPost), identity)
            }, {
                logd("onNext // modifyStarLabelsName")
                toast(getString(R.string.file_toolbar_star_filter_modify_name_success))
                initStarLabels()
            }) {
            logd("onError // modifyStarLabelsName")
            toast(getString(R.string.action_fail))
        }
    }

    /**
     * 删除星标
     * @param identity Int
     */
    private fun deleteLabel(identity: Int) {
        accessAPI(
            {
                deleteLabel(identity)
            }, {
                logd("onNext // deleteLabel")
                toast(getString(R.string.file_toolbar_star_delete_success))
                file_list_swipeLayout.autoRefresh()
            }) {
            logd("onError // deleteLabel")
            toast(getString(R.string.action_fail))
        }
    }

    override fun onBackPressed() {
        if (cab.isActive()) {
            cab.destroy()
        } else {
            if (parentPathList.size == 1) {
                super.onBackPressed()
            } else {
                nowOnPage = 0
                backToParentPath()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (settingSharedPreferences.getBoolean("first_run", true)) {
                initSpotlight()
            }
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {

//        toast("选了$color，颜色为${java.lang.String.format("#%06X", 0xFFFFFF and color)}")
        colorInt = (0xFFFFFF and color)
        colorButton.setBackgroundColor(color)
    }

    override fun onDialogDismissed(dialogId: Int) {}

    /**
     * 初始化 Aria
     * @param view View
     */
    private fun initAriaDownloader(view: View) {
        view.viewPager.adapter = TransmissionAdapter(this)
        TabLayoutMediator(view.tabLayout, view.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.transmission_download_title)
                    tab.icon = ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_baseline_cloud_download_24
                    )
                }
                else -> {
                    tab.text = getString(R.string.transmission_upload_title)
                    tab.icon = ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_baseline_cloud_upload_24
                    )
                }
            }
        }.attach()
    }

}