package com.jakting.rn6pan.activity.user

import android.content.DialogInterface
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
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.adapter.FileListAdapter
import com.jakting.rn6pan.api.data.FileLabelItem
import com.jakting.rn6pan.api.data.FileLabelList
import com.jakting.rn6pan.api.data.FileOrDirectoryList
import com.jakting.rn6pan.databinding.ActivityUserFileListBinding
import com.jakting.rn6pan.utils.*
import com.jakting.rn6pan.utils.MyApplication.Companion.ctimeOrderBy
import com.jakting.rn6pan.utils.MyApplication.Companion.defaultOrder
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_file_list.*
import kotlinx.android.synthetic.main.dialog_edittext.*
import kotlinx.android.synthetic.main.file_fab_create_folder_layout.*
import kotlinx.android.synthetic.main.file_fab_transmission_layout.*
import kotlinx.android.synthetic.main.file_fab_upload_layout.*
import kotlinx.android.synthetic.main.layout_target.*
import kotlinx.android.synthetic.main.layout_target.view.*
import okhttp3.MediaType
import okhttp3.RequestBody


class FileListActivity : BaseActivity(), ColorPickerDialogListener {
    var isShowFabMenu = false
    lateinit var fileLabelList: FileLabelList
    lateinit var adapter: FileListAdapter
    lateinit var mBinding: ActivityUserFileListBinding
    lateinit var dialogForLabelsList: AlertDialog
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
            R.id.menu_file_main_star -> {
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
                    arrayOfLabelList.add(
                        FileLabelItem(
                            fileLabel.name,
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
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val v = super.getView(position, convertView, parent)
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
                                    ColorPickerDialog.newBuilder().setColor(0xFF000000.toInt()).show(this@FileListActivity)
                                }
                                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                                    val editString = editInputLayout.text.toString()
                                    if (editString.trim().isNotEmpty()) {
                                        modifyStarLabelsName(
                                            arrayOfLabelList[position].identity,
                                            editString
                                        )
                                    } else {
                                        toast(getString(R.string.file_toolbar_star_filter_modify_name_fail))
                                    }
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
                        //Add margin between image and text (support various screen densities)
//                        val dp5 = (5 * resources.displayMetrics.density + 0.5f).toInt()
//                        logd("dp5 = $dp5")
//                        tv.compoundDrawablePadding = dp5

                        return v
                    }
                }


                dialogForLabelsList = MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.file_toolbar_star_filter))
                    .setAdapter(adapterDialog, { dialog, item ->

                    })
                    .setPositiveButton(R.string.file_toolbar_star_filter_add,
                        { dialogInterface: DialogInterface, i: Int ->

                        })
                    .setNeutralButton(R.string.file_toolbar_star_filter_clear,
                        { dialogInterface: DialogInterface, i: Int ->

                        })
                    .show()
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
            initStarLabels()
        }
//        initSpotlight()
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
            "{\"parentPath\":\"${parentPathList[parentPathList.size - 1]}\"," +
                    "\"limit\":9007199254740991,\"orderby\":[" +
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

    private fun initStarLabels() {
        val observable =
            EncapsulateRetrofit.init().getLabelsList()
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fileLabelList ->
                logd("onNext // getLabelsList")
                this.fileLabelList = fileLabelList
            }) { t ->
                logd("onError // getLabelsList")
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

    private fun initSpotlight() {
        val targets = ArrayList<Target>()
        val targetlayout = layoutInflater.inflate(R.layout.layout_target, FrameLayout(this))
        targetlayout.isClickable = true
        val backToParentTarget = Target.Builder()
            .setAnchor(getBottomBarItemView(bottomAppBar.navigationIcon)!!)
            .setShape(Circle(100f))
            .setEffect(RippleEffect(100f, 200f, argb(255, 72, 166, 151)))
            .setOverlay(targetlayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    targetlayout.spotlight_title.text =
                        getString(R.string.spotlight_backToParentTarget_title)
                    targetlayout.spotlight_content.text =
                        getString(R.string.spotlight_backToParentTarget_content)
                }

                override fun onEnded() {}
            })
            .build()
        targets.add(backToParentTarget)

        val fabTarget = Target.Builder()
            .setAnchor(file_fab)
            .setShape(Circle(100f))
            .setEffect(RippleEffect(100f, 200f, argb(255, 72, 166, 151)))
            .setOverlay(targetlayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    targetlayout.spotlight_title.text =
                        getString(R.string.spotlight_fabTarget_title)
                    targetlayout.spotlight_content.text =
                        getString(R.string.spotlight_fabTarget_content)
                }

                override fun onEnded() {}
            })
            .build()
        targets.add(fabTarget)

        val sortTarget = Target.Builder()
            .setAnchor(findViewById<View>(R.id.menu_file_sort))
            .setShape(Circle(100f))
            .setEffect(RippleEffect(100f, 200f, argb(255, 72, 166, 151)))
            .setOverlay(targetlayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    targetlayout.spotlight_title.text =
                        getString(R.string.spotlight_sortTarget_title)
                    targetlayout.spotlight_content.text =
                        getString(R.string.spotlight_sortTarget_content)
                    targetlayout.nextTargetButton.text = getString(R.string.spotlight_done)
                    (targetlayout.nextTargetButton as MaterialButton).icon =
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
        targetlayout.nextTargetButton.setOnClickListener { spotlight.next() }
    }

    private fun getBottomBarItemView(drawable: Drawable?): View? {
        val size: Int = bottomAppBar.childCount
        logd("获取底部栏的详情：size为$size")
        for (i in 0 until size) {
            val child: View = bottomAppBar.getChildAt(i)
            logd("获取底部栏的详情：view为$child")
            if (child is ImageButton) {
                if (child.drawable === drawable) {
                    logd("获取底部栏的详情：drawable${child.drawable}")
                    return child
                }
            }
        }
        return null
    }

    private fun modifyStarLabelsName(identity: Int, name: String) {
        val jsonForPost =
            "{\"name\":\"${name}\"}"
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"), jsonForPost
            )
        val observable =
            EncapsulateRetrofit.init().modifyLabelName(createDestinationPostBody, identity)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ FileLabel ->
                logd("onNext // modifyStarLabelsName")
                toast(getString(R.string.file_toolbar_star_filter_modify_name_success))
                initStarLabels()
            }) { t ->
                logd("onError // modifyStarLabelsName")
                t.printStackTrace()
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
        TODO("Not yet implemented")
    }

    override fun onDialogDismissed(dialogId: Int) {
        TODO("Not yet implemented")
    }
}