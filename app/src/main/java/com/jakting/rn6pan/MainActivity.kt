package com.jakting.rn6pan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.jakting.rn6pan.activity.common.AboutActivity
import com.jakting.rn6pan.activity.common.SettingsActivity
import com.jakting.rn6pan.activity.user.FileListActivity
import com.jakting.rn6pan.activity.user.LoginActivity
import com.jakting.rn6pan.activity.user.UserActivity
import com.jakting.rn6pan.utils.*
import com.jakting.rn6pan.utils.CookiesUtils.Companion.saveCookie
import com.jakting.rn6pan.utils.MyApplication.Companion.COOKIES
import com.jakting.rn6pan.utils.MyApplication.Companion.DESTINATION
import com.jakting.rn6pan.utils.MyApplication.Companion.LOGIN_STATUS
import com.jakting.rn6pan.utils.MyApplication.Companion.STATE
import com.jakting.rn6pan.utils.MyApplication.Companion.TOKEN
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat


class MainActivity : BaseActivity(), View.OnClickListener {

    companion object {
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"),
                "{\"ts\":123}"
            )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_main_refresh->{
                getUserInfo()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        getUserInfo()
        init()
    }

    private fun init() {
        main_check_login_status_card.setOnClickListener(this)
        main_file_manager_card.setOnClickListener(this)
        main_offline_download_card.setOnClickListener(this)
        main_ticket_layout.setOnClickListener(this)
        main_setting_layout.setOnClickListener(this)
        main_about_layout.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun getUserInfo() {
        val observable = EncapsulateRetrofit.init().getUserInfo(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ userInfo ->
                logd("onNext // getUserInfo")
                LOGIN_STATUS = 1
                MyApplication.userInfo = userInfo
                main_check_login_status_card.setCardBackgroundColor(
                    ContextCompat.getColor(
                        MyApplication.appContext,
                        R.color.colorGreen1
                    )
                )
                main_file_manager_card.setCardBackgroundColor(
                    ContextCompat.getColor(
                        MyApplication.appContext,
                        R.color.colorGreen2
                    )
                )
                main_offline_download_card.setCardBackgroundColor(
                    ContextCompat.getColor(
                        MyApplication.appContext,
                        R.color.colorGreen3
                    )
                )
                main_check_login_status_card.isClickable = true
                main_check_login_status_icon.visibility = View.VISIBLE
                main_check_login_status_icon.setImageDrawable(
                    ContextCompat.getDrawable(
                        MyApplication.appContext,
                        R.drawable.ic_baseline_face_24
                    )
                )
                logd("userInfo.icon：    " + userInfo.icon)
                Glide.with(MyApplication.appContext).load(userInfo.icon)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(main_check_login_status_userIcon)
                main_check_login_status_progressbar.visibility = View.INVISIBLE
                main_check_login_status_title.text = userInfo.name
                main_check_login_status_second_title.text =
                    if (userInfo.vip != 0) (getString(R.string.main_check_login_status_sub_until) +
                            SimpleDateFormat("yyyy-MM-dd").format(
                                userInfo.vipExpireTime
                            )) else getString(R.string.main_check_login_status_sub_expires)
                main_file_manager_second_title.text =
                    "配额（" + getPrintSize(userInfo.spaceUsed) + "/" + getPrintSize(userInfo.spaceCapacity) + "）"
                getOfflineQuota()
            }) { t ->
                logd("onError // getUserInfo")
                t.printStackTrace()
                createDestination()
            }
    }

    private fun createDestination() {
        val observable = EncapsulateRetrofit.init().getDestination(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ createDestination ->
                logd("onNext // createDestination")
                logd("destination:  " + createDestination.destination)
                logd("expireTime:   " + createDestination.expireTime.toString())
                DESTINATION = createDestination.destination
                checkDestination(DESTINATION)
            }) { t ->
                logd("onError // createDestination")
                t.printStackTrace()
                toast(getString(R.string.action_fail))
            }
    }

    private fun checkDestination(destination: String) {
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"),
                "{\"destination\":\"$destination\"}"
            )
        val observable = EncapsulateRetrofit.init().getToken(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ checkDestination ->
                logd("onNext // checkDestination")
                if (checkDestination.status != 100) {
                    //未登录
                    LOGIN_STATUS = 0
                    main_check_login_status_card.isClickable = true
                    main_check_login_status_icon.visibility = View.VISIBLE
                    main_check_login_status_progressbar.visibility = View.INVISIBLE
                    main_check_login_status_title.text =
                        getString(R.string.main_check_login_status_title_error)
                    main_check_login_status_second_title.text =
                        getString(R.string.main_check_login_status_second_title_error)
                } else {
                    //登录了
                    LOGIN_STATUS = 1
                    STATE = checkDestination.state
                    TOKEN = checkDestination.token
                    getUserInfo()
                }
            }) { t ->
                logd("onError // checkDestination")
                t.printStackTrace()
                toast(getString(R.string.action_fail))
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getOfflineQuota() {
        val observable = EncapsulateRetrofit.init().getOfflineQuota(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ offlineQuota ->
                logd("onNext // OfflineQuota")
                main_offline_download_second_title.text =
                    "配额（今日已用 ${offlineQuota.dailyUsed} 次，剩余 ${offlineQuota.available} 次/ ${offlineQuota.dailyQuota} 次）"
            }) { t ->
                logd("onError // OfflineQuota")
                t.printStackTrace()
                toast(getString(R.string.action_fail))
            }
    }

    override fun onClick(p0: View?) {
        when (p0) {
            main_check_login_status_card -> {
                if (LOGIN_STATUS == 0) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivityForResult(intent, INTENT_ACTIVITY_LOGIN_CODE)
                } else if (LOGIN_STATUS == 1) {
                    val intent = Intent(this, UserActivity::class.java)
                    startActivity(intent)
                }

            }
            main_file_manager_card->{
                val intent = Intent(this, FileListActivity::class.java)
                startActivity(intent)

            }
            main_offline_download_card->{

            }
            main_ticket_layout->{

            }
            main_setting_layout->{
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            main_about_layout->{
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            INTENT_ACTIVITY_LOGIN_CODE -> if (resultCode == RESULT_OK) {
                LOGIN_STATUS = 1
                saveCookie(API_NUDE_URL, COOKIES)
                getUserInfo()
            }
        }
    }
}