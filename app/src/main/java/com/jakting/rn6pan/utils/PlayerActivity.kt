package com.jakting.rn6pan.utils

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager


class PlayerActivity : BaseActivity() {
    var videoPlayer: StandardGSYVideoPlayer? = null
    var orientationUtils: OrientationUtils? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_play)
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
        init()
    }

    private fun init() {
        videoPlayer = findViewById<View>(R.id.video_player) as StandardGSYVideoPlayer
        //增加title
        videoPlayer!!.titleTextView.visibility = View.VISIBLE
        //设置返回键
        videoPlayer!!.backButton.visibility = View.VISIBLE
        //设置旋转
        orientationUtils = OrientationUtils(this, videoPlayer)
        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        videoPlayer!!.fullscreenButton.setOnClickListener {
            orientationUtils!!.resolveByClick()
            videoPlayer!!.startWindowFullscreen(this, true, true)
        }
        //是否可以滑动调整
        videoPlayer!!.setIsTouchWiget(true)
        //根据长宽自动旋转
        videoPlayer!!.isAutoFullWithSize = true
        //设置返回按键功能
        videoPlayer!!.backButton.setOnClickListener {
            onBackPressed()
        }
        val videoIdentity = intent.getStringExtra("identity")
        val jsonForPost = "{\"identity\":\"$videoIdentity\"}"
        val createDestinationPostBody =
            RequestBody.create(
                MediaType.parse("application/json"), jsonForPost
            )
        val observable =
            EncapsulateRetrofit.init().getVideoPreview(createDestinationPostBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ videoPreview ->
                logd("onNext // getVideoPreview")
                videoPlayer!!.setUp(videoPreview.playAddress, true, videoPreview.title)
                videoPlayer!!.startPlayLogic()
            }) { t ->
                logd("onError // getVideoPreview")
                val errorString: String = getErrorString(t)
                logd(errorString)
            }
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
        if (orientationUtils != null) orientationUtils!!.releaseListener()
    }

    override fun onBackPressed() {
        //先返回正常状态
        if (orientationUtils!!.screenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoPlayer!!.fullscreenButton.performClick()
            return
        }
        //释放所有
        videoPlayer!!.setVideoAllCallBack(null)
        super.onBackPressed()
    }
}