package com.jakting.rn6pan.activity.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakting.rn6pan.R
import com.jakting.rn6pan.utils.MyApplication.Companion.appContext
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import kotlin.math.abs


/**
 * Created by shuyu on 2016/12/7.
 * 注意
 * 这个播放器的demo配置切换到全屏播放器
 * 这只是单纯的作为全屏播放显示，如果需要做大小屏幕切换，请记得在这里耶设置上视频全屏的需要的自定义配置
 */
class SampleControlVideo : StandardGSYVideoPlayer {
    private var mChangeRatio: TextView? = null  //显示比例
    private var mChangeRotate: TextView? = null
    //private var mChangePlaySpeed: TextView? = null

    //记住切换数据源类型
    private var mRatioType = 0
    private var mTransformSize = 0
    //private var mPlaySpeed = 1F

    //数据源
    private var mSourcePosition = 0

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    constructor(context: Context?, fullFlag: Boolean?) : super(context, fullFlag) {}
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun init(context: Context) {
        super.init(context)
        initView()
    }

    private fun initView() {
        mChangeRatio = findViewById<View>(R.id.change_ratio) as TextView
        mChangeRotate = findViewById<View>(R.id.change_rotate) as TextView
        //mChangePlaySpeed = findViewById<View>(R.id.change_playspeed) as TextView


        //切换显示比例
        mChangeRatio!!.setOnClickListener {
            if (!mHadPlay) {
                return@setOnClickListener
            }
            mRatioType = when (mRatioType) {
                0 -> 1
                1 -> 2
                2 -> 3
                3 -> 4
                4 -> 0
                else -> 0
            }
            resolveTypeUI()
        }

        //旋转播放角度
        mChangeRotate!!.setOnClickListener {
            if (!mHadPlay) {
                return@setOnClickListener
            }
            if (mTextureView.rotation - mRotate == 270f) {
                mTextureView.rotation = mRotate.toFloat()
                mTextureView.requestLayout()
            } else {
                mTextureView.rotation = mTextureView.rotation + 90
                mTextureView.requestLayout()
            }
        }

    }

    /**
     * 处理显示逻辑
     */
    override fun onSurfaceAvailable(surface: Surface) {
        super.onSurfaceAvailable(surface)
        resolveRotateUI()
    }

    override fun getLayoutId(): Int {
        return R.layout.player_control_video
    }

    /**
     * 全屏时将对应处理参数逻辑赋给全屏播放器
     *
     * @param context
     * @param actionBar
     * @param statusBar
     * @return
     */
    override fun startWindowFullscreen(
        context: Context,
        actionBar: Boolean,
        statusBar: Boolean
    ): GSYBaseVideoPlayer {
        val sampleVideo =
            super.startWindowFullscreen(context, actionBar, statusBar) as SampleControlVideo
        sampleVideo.mSourcePosition = mSourcePosition
        sampleVideo.mRatioType = mRatioType
        sampleVideo.mTransformSize = mTransformSize
        //sampleVideo.resolveTransform();
        sampleVideo.resolveTypeUI()
        //sampleVideo.resolveRotateUI();
        //这个播放器的demo配置切换到全屏播放器
        //这只是单纯的作为全屏播放显示，如果需要做大小屏幕切换，请记得在这里耶设置上视频全屏的需要的自定义配置
        //比如已旋转角度之类的等等
        //可参考super中的实现
        return sampleVideo
    }

    /**
     * 推出全屏时将对应处理参数逻辑返回给非播放器
     *
     * @param oldF
     * @param vp
     * @param gsyVideoPlayer
     */
    override fun resolveNormalVideoShow(oldF: View, vp: ViewGroup, gsyVideoPlayer: GSYVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
        val sampleVideo = gsyVideoPlayer as SampleControlVideo
        mSourcePosition = sampleVideo.mSourcePosition
        mRatioType = sampleVideo.mRatioType
        mTransformSize = sampleVideo.mTransformSize
        resolveTypeUI()
    }

    /**
     * 旋转逻辑
     */
    private fun resolveRotateUI() {
        if (!mHadPlay) {
            return
        }
        mTextureView.rotation = mRotate.toFloat()
        mTextureView.requestLayout()
    }

    /**
     * 显示比例
     * 注意，GSYVideoType.setShowType是全局静态生效，除非重启APP。
     */
    private fun resolveTypeUI() {
        if (!mHadPlay) {
            return
        }
        when (mRatioType) {
            1 -> {
                mChangeRatio!!.text = appContext.getString(R.string.player_ratio_16_9)
                GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9)
            }
            2 -> {
                mChangeRatio!!.text = appContext.getString(R.string.player_ratio_4_3)
                GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3)
            }
            3 -> {
                mChangeRatio!!.text = appContext.getString(R.string.player_ratio_full)
                GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL)
            }
            4 -> {
                mChangeRatio!!.text = appContext.getString(R.string.player_ratio_truly_full)
                GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL)
            }
            0 -> {
                mChangeRatio!!.text = appContext.getString(R.string.player_ratio_default)
                GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
            }
        }
        changeTextureViewShowType()
        if (mTextureView != null) mTextureView.requestLayout()
    }

    override fun touchSurfaceMove(deltaX: Float, deltaY: Float, y: Float) {
        var curWidth = 0
        var curHeight = 0
        if (activityContext != null) {
            curWidth =
                if (CommonUtil.getCurrentScreenLand(activityContext as Activity)) mScreenHeight else mScreenWidth
            curHeight =
                if (CommonUtil.getCurrentScreenLand(activityContext as Activity)) mScreenWidth else mScreenHeight
        }
        if (mChangePosition) {
            val totalTimeDuration = duration
            mSeekTimePosition =
                (mDownPosition + deltaX * totalTimeDuration / curWidth / mSeekRatio).toInt()
            if (mSeekTimePosition > totalTimeDuration) mSeekTimePosition = totalTimeDuration
            val seekTime = CommonUtil.stringForTime(mSeekTimePosition)
            val totalTime = CommonUtil.stringForTime(totalTimeDuration)
            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration)
        } else if (mChangeVolume) {
            val deltaYY = -deltaY
            val max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val deltaV = (max * deltaYY * 3 / curHeight).toInt()
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0)
            val volumePercent =
                (mGestureDownVolume * 100 / max + deltaYY * 3 * 100 / curHeight).toInt()
            showVolumeDialog(-deltaY, volumePercent)
        } else if (mBrightness) {
            if (abs(deltaY) > mThreshold) {
                val percent = -deltaY / curHeight / 0.25f  //0.25f是手动调节的滑动比例
                onBrightnessSlide(percent)
                mDownY = y
            }
        }
    }
}