package com.jakting.rn6pan.api

import com.jakting.rn6pan.api.data.*
import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiParse {

    /*
        登录请求
     */
    // v3/15.统一认证/20.使用轮询token回调登录.md
    //请求 Destination
    @POST("user/createDestination")
    fun getDestination(@Body requestBody: RequestBody): Observable<createDestination>

    // v3/15.统一认证/20.使用轮询token回调登录.md
    //借助 Destination 检查当前登录状态并获得 Token
    @POST("user/checkDestination")
    fun getToken(@Body requestBody: RequestBody): Observable<checkDestination>

    /*
        用户信息
     */
    //v3/10.用户/30.拉取当前用户信息.md
    //获取当前用户信息
    @POST("user/info")
    fun getUserInfo(@Body requestBody: RequestBody): Observable<UserInfo>

    /*
        文件
     */
    //v3/21.新文件/030.列出文件（含搜索）.md
    //列出文件夹下所有文件
    @POST("newfile/list")
    fun getFileOrDirectoryList(@Body requestBody: RequestBody): Observable<FileOrDirectoryList>

    //v3/21.新文件/010.创建文件夹.md
    //新建文件夹
    @POST("newfile")
    fun createDirectory(@Body requestBody: RequestBody): Observable<FileOrDirectory>

    /*
        预览
     */
    //v3/30.预览/80.预览图片地址.md
    //查询图片预览地址
    @POST("preview/image")
    fun getImagePreview(@Body requestBody: RequestBody): Observable<PicturePreview>

    //v3/30.预览/80.预览图片地址.md
    //查询视频预览地址
    @POST("preview/video")
    fun getVideoPreview(@Body requestBody: RequestBody): Observable<VideoPreview>

    //v3/30.预览/80.预览图片地址.md
    //查询音频预览地址
    @POST("preview/audio")
    fun getAudioPreview(@Body requestBody: RequestBody): Observable<AudioPreview>

    /*
        离线任务
     */

    //v3/50.离线任务/000.100.离线任务配额.md
    //获取离线任务配额
    @POST("offline/quota")
    fun getOfflineQuota(@Body requestBody: RequestBody): Observable<OfflineQuota>

}