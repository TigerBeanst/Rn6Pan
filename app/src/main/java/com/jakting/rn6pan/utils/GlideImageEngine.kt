package com.jakting.rn6pan.utils

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.jakting.rn6pan.R
import com.maning.imagebrowserlibrary.ImageEngine


class GlideImageEngine : ImageEngine {
    override fun loadImage(
        context: Context,
        url: String?,
        imageView: ImageView,
        progressView: View,
        customImageView: View
    ) {
        Glide.with(context)
            .asBitmap()
            .load(url)
            .apply(
                RequestOptions().fitCenter().error(R.mipmap.ic_launcher)
                    .placeholder(R.drawable.default_placeholder)
            )
            .listener(object : RequestListener<Bitmap?> {
                override fun onLoadFailed(
                    @Nullable e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressView.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressView.visibility = View.GONE
                    return false
                }
            })
            .into(imageView)
    }
}