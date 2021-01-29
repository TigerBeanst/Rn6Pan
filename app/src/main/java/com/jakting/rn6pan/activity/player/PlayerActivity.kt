package com.jakting.rn6pan.activity.player

import android.os.Bundle
import android.view.View
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.utils.EncapsulateRetrofit
import com.jakting.rn6pan.utils.MyApplication.Companion.nowTimeStamp
import com.jakting.rn6pan.utils.getErrorString
import com.jakting.rn6pan.utils.logd
import com.jakting.rn6pan.utils.toast
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.player_control_video.view.*
import okhttp3.MediaType
import okhttp3.RequestBody


class PlayerActivity : BaseActivity() {
    var videoPlayer: StandardGSYVideoPlayer? = null
    var orientationUtils: OrientationUtils? = null
    private val mActivity = this
    var playSpeed = 1F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_play)
        val decorView: View = this.window.decorView
        PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
        CacheFactory.setCacheManager(ProxyCacheManager::class.java)
        init()
    }

    private fun init() {
        videoPlayer = findViewById<View>(R.id.video_player) as SampleControlVideo
        //增加title
        videoPlayer!!.titleTextView.visibility = View.VISIBLE
        //设置返回键
        videoPlayer!!.backButton.visibility = View.VISIBLE
        //设置返回按键功能
        videoPlayer!!.backButton.setOnClickListener {
            onBackPressed()
        }
        videoPlayer!!.isIfCurrentIsFullscreen = true
        //设置变速
        videoPlayer!!.change_playspeed.text =
            String.format(getString(R.string.player_change_play_speed), playSpeed)
        videoPlayer!!.change_playspeed.setOnClickListener {
            playSpeed = when (playSpeed) {
                1f -> 1.5f
                1.5f -> 2f
                2f -> 0.5f
                0.5f -> 0.25f
                0.25f -> 1f
                else -> 1f
            }
            videoPlayer!!.change_playspeed.text =
                String.format(getString(R.string.player_change_play_speed), playSpeed)
            videoPlayer!!.setSpeed(playSpeed, true)
        }
//        videoPlayer!!.setBackFromFullScreenListener {
//            mActivity.onBackPressed()
//        }
        //设置旋转
        orientationUtils = OrientationUtils(this, videoPlayer)
        //是否可以滑动调整
        videoPlayer!!.setIsTouchWiget(true)
        //滑动比例（阻尼）
        videoPlayer!!.seekRatio = 4F
        //根据长宽自动旋转
        videoPlayer!!.isAutoFullWithSize = true
        videoPlayer!!.setBottomProgressBarDrawable(null)

        val videoIdentity = intent.getStringExtra("identity")
        getVideoPreviewURLByDownloadAddress(videoIdentity!!)
    }

    private fun getVideoPreviewURLByDownloadAddress(videoIdentity: String) {
        val jsonForPost = "{\"identity\":\"$videoIdentity\"}"
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"), jsonForPost
            )
        val observable =
            EncapsulateRetrofit.init().getDownloadAddress(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fileOrDirectory ->
                logd("onNext // getVideoPreviewURLByDownloadAddress")
                nowTimeStamp = System.currentTimeMillis() / 1000
                startPlay(fileOrDirectory.downloadAddress, fileOrDirectory.name)
            }) { t ->
                logd("onError // getVideoPreviewURLByDownloadAddress")
                val errorString: String = getErrorString(t)
                logd(errorString)
                toast(getString(R.string.action_fail))
            }
    }


    private fun startPlay(playAddress: String, title: String) {
        logd("此时播放地址：$playAddress")
        videoPlayer!!.setUp(playAddress, true, title)
        videoPlayer!!.startPlayLogic()
    }



    override fun onPause() {
        super.onPause()
        videoPlayer!!.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        videoPlayer!!.onVideoResume()
    }


    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
//        if (orientationUtils != null) orientationUtils!!.releaseListener()
    }


    override fun onBackPressed() {
        //先返回正常状态。如果屏幕是横屏，则模拟触碰全屏按钮
//        if (orientationUtils!!.screenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//            videoPlayer!!.fullscreenButton.performClick()
//            return
//        }
        //释放所有
        videoPlayer!!.setVideoAllCallBack(null)
        super.onBackPressed()
    }
}