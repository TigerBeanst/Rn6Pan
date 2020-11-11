package com.jakting.rn6pan.api.data

data class PicturePreview(
//    val height:Int,
//    val width:Int,
//    val sourceHeight:Int,
//    val sourceWidth:Int,
//    val title:String,
    val playAddress:String
)

data class VideoPreview(
    val height:Int,
    val width:Int,
    val rotate:Int,
    val duration:Int,
    val sourceHeight:Int,
    val sourceWidth:Int,
    val title:String,
    val playAddress:String
)

data class AudioPreview(
    val height:Int,
    val width:Int,
    val rotate:Int,
    val duration:Int,
    val sourceHeight:Int,
    val sourceWidth:Int,
    val title:String,
    val playAddress:String
)