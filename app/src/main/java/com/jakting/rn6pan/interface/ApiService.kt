package com.jakting.rn6pan.`interface`

import com.jakting.rn6pan.utils.*
import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {

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

    /*
        离线任务
     */

    //v3/50.离线任务/000.100.离线任务配额.md
    //获取离线任务配额
    @POST("offline/quota")
    fun getOfflineQuota(@Body requestBody: RequestBody): Observable<OfflineQuota>

}