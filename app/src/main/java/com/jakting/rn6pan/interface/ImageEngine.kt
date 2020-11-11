package com.jakting.rn6pan.`interface`

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jakting.rn6pan.R


interface ImageEngine {
    /**
     * 加载图片方法
     *
     * @param context         上下文
     * @param url             图片地址
     * @param imageView       ImageView
     * @param progressView    进度View
     * @param customImageView 自定义加载图片，替换PhotoView
     */
    fun loadImage(
        context: Context,
        url: String,
        imageView: ImageView,
        progressView: View,
        customImageView: View
    )

    //Glide
    class GlideImageEngine : ImageEngine {
        override fun loadImage(
            context: Context,
            url: String,
            imageView: ImageView,
            progressView: View,
            customImageView: View
        ) {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .fitCenter()
                .placeholder(R.drawable.default_placeholder)
                .error(R.mipmap.ic_launcher)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressView.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressView.visibility = View.GONE
                        return false
                    }
                })
                .into(imageView)
        }
    }
}