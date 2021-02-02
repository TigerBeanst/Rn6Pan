package com.jakting.rn6pan.api

import com.jakting.rn6pan.api.data.*
import com.jakting.rn6pan.utils.EncapsulateRetrofit
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * 再封装 RxJava+Retrofit
 *
 * @param useAPI
 * @param onSuccess
 * @param onError
 */
fun accessAPI(
    useAPI: ApiParse.() -> Any,
    onSuccess: (Any) -> Unit,
    onError: (Throwable) -> Unit
) {
    val observable =
        EncapsulateRetrofit.init().useAPI() as Observable<*>
    observable.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ AnyApiObject ->
            onSuccess(AnyApiObject)
        }) { t ->
            t.printStackTrace()
            onError(t)
        }
}

/**
 * DEMO
accessAPI(
{
getFileOrDirectoryList(getPostBody(jsonForPost))
}, { objectReturn ->
val fileOrDirectoryList = objectReturn as FileOrDirectoryList
logd("onNext // getFileOrDirectoryList")
nowFileOrDirectoryList = fileOrDirectoryList
setFileListAdapter()
}) {
logd("onError // getFileOrDirectoryList")
toast(getString(R.string.action_fail))
}
 */

interface ApiParse {

    /*
    杂项请求
     */
    @GET("version.json")
    fun getUpdate(): Observable<AppUpdateData>

    /*
    登录请求
     */

    /**
     * v3/15.统一认证/20.使用轮询token回调登录.md 「申请登录状态检查令牌」
     * @param requestBody RequestBody
     * @return Observable<createDestination>
     */
    @POST("user/createDestination")
    fun getDestination(@Body requestBody: RequestBody): Observable<createDestination>

    /**
     * v3/15.统一认证/20.使用轮询token回调登录.md 「使用 token 检查用户登录状态」
     * @param requestBody RequestBody
     * @return Observable<checkDestination>
     */
    @POST("user/checkDestination")
    fun getToken(@Body requestBody: RequestBody): Observable<checkDestination>

    /*
    用户信息
     */

    /**
     * v3/10.用户/30.拉取当前用户信息.md 「拉取当前用户信息（判断是否登录）」
     * @param requestBody RequestBody
     * @return Observable<UserInfo>
     */
    @POST("user/info")
    fun getUserInfo(@Body requestBody: RequestBody): Observable<UserInfo>

    /*
    文件
     */

    /**
     * v3/21.新文件/030.列出文件（含搜索）.md 「列出文件夹下所有文件」
     * @param requestBody RequestBody
     * @return Observable<FileOrDirectoryList>
     */
    @POST("newfile/list")
    fun getFileOrDirectoryList(@Body requestBody: RequestBody): Observable<FileOrDirectoryList>

    /**
     * v3/21.新文件/（旧的有，新的没有文档） 「获取文件下载地址」
     * @param requestBody RequestBody
     * @return Observable<FileOrDirectory>
     */
    @POST("newfile/download")
    fun getDownloadAddress(@Body requestBody: RequestBody): Observable<FileOrDirectory>

    /**
     * v3/21.新文件/040.重命名文件.md 「重命名文件或文件夹」
     * @param requestBody RequestBody
     * @return Observable<FileActionReturn>
     */
    @POST("newfile/rename")
    fun renameFile(@Body requestBody: RequestBody): Observable<FileActionReturn>

    /**
     * v3/21.新文件/010.创建文件夹.md 「创建新的文件夹」
     * @param requestBody RequestBody
     * @return Observable<FileOrDirectory>
     */
    @POST("newfile")
    fun createDirectory(@Body requestBody: RequestBody): Observable<FileOrDirectory>

    /**
     * v3/21.新文件/080.文件星标管理.md 「列出现有星标列表」
     * @return Observable<FileLabelList>
     */
    @GET("labels?skip=0&limit=9007199254740991")
    fun getLabelsList(): Observable<FileLabelList>

    /**
     * v3/21.新文件/080.文件星标管理.md 「修改星标（目前仅支持修改名字）」
     * @param requestBody RequestBody
     * @param identity Int
     * @return Observable<FileLabel>
     */
    @PUT("labels/{identity}")
    fun modifyLabelName(
        @Body requestBody: RequestBody,
        @Path("identity") identity: Int
    ): Observable<FileLabel>

    /**
     * v3/21.新文件/080.文件星标管理.md 「删除星标」
     * @param identity Int
     * @return Observable<FileActionReturn>
     */
    @DELETE("labels/{identity}")
    fun deleteLabel(@Path("identity") identity: Int): Observable<FileActionReturn>

    /**
     * v3/21.新文件/080.文件星标管理.md 「创建文件星标」
     * @param requestBody RequestBody
     * @return Observable<FileLabel>
     */
    @POST("labels")
    fun createLabel(@Body requestBody: RequestBody): Observable<FileLabel>

    /**
     * v3/21.新文件/090.给文件添加或删除星标.md 「为指定文件/文件夹->添加星标」
     * @param requestBody RequestBody
     * @return Observable<FileActionReturn>
     */
    @POST("newfile/addLabel")
    fun addLabel(@Body requestBody: RequestBody): Observable<FileActionReturn>

    /**
     * v3/21.新文件/090.给文件添加或删除星标.md 「为指定文件/文件夹->移除星标」
     * @param requestBody RequestBody
     * @return Observable<FileActionReturn>
     */
    @POST("newfile/removeLabel")
    fun removeLabel(@Body requestBody: RequestBody): Observable<FileActionReturn>

    /*
    离线任务
     */

    /**
     * v3/50.离线任务/000.100.离线任务配额.md 「获取离线任务配额」
     * @param requestBody RequestBody
     * @return Observable<OfflineQuota>
     */
    @POST("offline/quota")
    fun getOfflineQuota(@Body requestBody: RequestBody): Observable<OfflineQuota>

    /**
     * v3/50.离线任务/000.080.列出离线任务.md 「列出文件夹下所有文件」
     * @param requestBody RequestBody
     * @return Observable<OfflineList>
     */
    @POST("offline/list")
    fun getOfflineList(@Body requestBody: RequestBody): Observable<OfflineList>

    /**
     * v3/50.离线任务/000.020.10.预解析链接.md 「预解析链接」
     * @param requestBody RequestBody
     * @return Observable<OfflineParse>
     */
    @POST("offline/parse")
    fun getOfflineParse(@Body requestBody: RequestBody): Observable<OfflineParse>

    /**
     * v3/50.离线任务/000.050.添加离线任务.md 「添加离线任务」
     * @param requestBody RequestBody
     * @return Observable<OfflineAddResponse>
     */
    @POST("offline/add")
    fun addOfflineTask(@Body requestBody: RequestBody): Observable<OfflineAddResponse>

}