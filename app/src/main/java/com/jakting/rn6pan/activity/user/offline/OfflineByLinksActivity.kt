package com.jakting.rn6pan.activity.user.offline

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.common.ChoosePathActivity
import com.jakting.rn6pan.adapter.OfflineByLinksListAdapter
import com.jakting.rn6pan.api.accessAPI
import com.jakting.rn6pan.api.data.*
import com.jakting.rn6pan.utils.getErrorString
import com.jakting.rn6pan.utils.getPostBody
import com.jakting.rn6pan.utils.logd
import com.jakting.rn6pan.utils.toast
import kotlinx.android.synthetic.main.activity_offline_by_links.*

class OfflineByLinksActivity : AppCompatActivity() {

    lateinit var offlineByLinksListAdapter: OfflineByLinksListAdapter
    private var needParseList = ArrayList<String>()
    var needParseListIndex = 0
    var needParseListSize = 0
    var pathForDownload = "/"
    var nowParseList = ArrayList<OfflineParse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_by_links)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(R.string.offline_new_by_links)
        initLinksEditText()
        initButtonClick()
    }

    /**
     * 初始化输入框
     */
    private fun initLinksEditText() {
        offline_by_links_editText_links.isFocusable = true
        offline_by_links_editText_links.requestFocus()
        offline_new_by_links_path_text.text = pathForDownload
    }

    /**
     * 初始化按钮的点击事件
     */
    private fun initButtonClick() {
        offline_new_by_links_button_parse.setOnClickListener {
            offline_new_by_links_button_submit.isEnabled = true
            val originLinks = offline_by_links_editText_links.text!!
            if (originLinks.isNotEmpty()) {
                needParseList.clear()
                val originLinksArray = originLinks.split("\n")
                needParseListIndex = 0
                for (originLink in originLinksArray) {
                    needParseList.add(isAndParseThunderLink(originLink))
                }
                val needParseIt: MutableIterator<String> = needParseList.iterator()
                while (needParseIt.hasNext()) {
                    val needParse = needParseIt.next()
                    if (nowParseList.any { it.info.textLink == needParse }) {
                        needParseIt.remove()
                    }
                }
                needParseListSize = needParseList.size
                if (needParseListSize > 0) {
                    parseOfflineLink(
                        needParseList[needParseListIndex],
                        offline_by_links_editText_username.text.toString(),
                        offline_by_links_editText_password.text.toString()
                    )
                }

            } else {
                toast(getString(R.string.offline_new_by_links_button_parse_empty))
            }
        }
        offline_new_by_links_button_submit.setOnClickListener {
            if (nowParseList.isNotEmpty()) {
                addOfflineTask()
            } else {
                toast(getString(R.string.offline_new_by_links_button_submit_empty))
            }
        }
        offline_new_by_links_path_button.setOnClickListener {
            val intent = Intent(this, ChoosePathActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }

    /**
     * 返回设置路径的结果
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 1) {
            pathForDownload = data?.getStringExtra("choose_path")!!
            offline_new_by_links_path_text.text = pathForDownload
        }
    }

    /**
     * 判断 & 解析 迅雷链接
     * @param textLink String
     * @return String
     */
    private fun isAndParseThunderLink(textLink: String): String {
        return if (textLink.startsWith("thunder://")) {
            //迅雷链接
            val tempLink = textLink.substring(10) //切断「thunder://」
            val decodeLink = String(Base64.decode(tempLink, Base64.DEFAULT)) //解码 Base64
            decodeLink.substring(2, decodeLink.length - 2) //切割头尾的 AA 和 ZZ
        } else {
            textLink
        }
    }

    /**
     * 设置预解析列表解析器
     */
    private fun setParseListAdapter() {
        val layoutManager = LinearLayoutManager(this)
        offline_new_by_links_task_recyclerview.layoutManager = layoutManager
        offlineByLinksListAdapter = OfflineByLinksListAdapter(nowParseList, this)
        offline_new_by_links_task_recyclerview.adapter = offlineByLinksListAdapter
    }

    /**
     * 预解析任务（通过 URL）
     * @param textLink String
     * @param username String
     * @param password String
     */
    private fun parseOfflineLink(
        textLink: String,
        username: String,
        password: String
    ) {
        val jsonForPost =
            "{\"textLink\":\"$textLink\"" +
                    if (username.isNotEmpty()) {
                        ",\"username\":$username"
                    } else {
                        ""
                    } +
                    if (password.isNotEmpty()) {
                        ",\"password\":$password"
                    } else {
                        ""
                    } +
                    "}"
        accessAPI(
            {
                getOfflineParse(getPostBody(jsonForPost))
            }, { objectReturn ->
                val offlineParse = objectReturn as OfflineParse
                nowParseList.add(offlineParse)
                continueParse()
                logd("onNext // parseOfflineLink")
            }) { t ->
            t.printStackTrace()
            val jsonFromError = Gson().fromJson(getErrorString(t), OfflineParseError::class.java)
            if (jsonFromError.reference == "UNSUPPORT_URL") {
                MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.offline_new_by_links_button_parse_error) + textLink)
                    .setPositiveButton(getString(R.string.ok)) { _, _ -> }
                    .show()
            }
            continueParse()
        }
    }

    private fun continueParse() {
        needParseListIndex++
        if (needParseListSize > needParseListIndex) {
            //因为大于一条，没有必要再解析是否有用户名密码
            parseOfflineLink(
                needParseList[needParseListIndex],
                "",
                ""
            )
        } else {
            setParseListAdapter()
        }
    }

    /**
     * 添加离线任务任务（通过 URL）
     */
    private fun addOfflineTask() {
        val offlineAdd = OfflineAdd(ArrayList<OfflineAddTaskInfo>(), "/")
        for (nowParse in nowParseList) {
            offlineAdd.task.add(OfflineAddTaskInfo(nowParse.hash))
        }
        offlineAdd.savePath = pathForDownload
        val jsonForPost = Gson().toJson(offlineAdd)
        println(jsonForPost)
        accessAPI(
            {
                addOfflineTask(getPostBody(jsonForPost))
            }, { objectReturn ->
                val offlineAddResponse = objectReturn as OfflineAddResponse
                toast(
                    String.format(
                        getString(R.string.offline_new_by_links_button_submit_ok),
                        offlineAddResponse.successCount
                    )
                )
                val intent = intent
                intent.putExtra("isSubmit",true)
                setResult(1,intent)
                finishAfterTransition()
                logd("onNext // parseOfflineLink")
            }) { t ->
            t.printStackTrace()
            toast(getString(R.string.action_fail))
        }
    }

}