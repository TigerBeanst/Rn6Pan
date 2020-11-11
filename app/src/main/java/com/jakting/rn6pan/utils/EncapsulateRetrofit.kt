package com.jakting.rn6pan.utils

import com.github.simonpercic.oklog3.OkLogInterceptor
import com.jakting.rn6pan.BuildConfig
import com.jakting.rn6pan.`interface`.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class EncapsulateRetrofit {
    companion object {
        fun init(): ApiService {

            val okHttpBuilder = OkHttpClient.Builder()
            if (BuildConfig.DEBUG) {
                val okLogInterceptor = OkLogInterceptor.builder().build()
                okHttpBuilder.addInterceptor(okLogInterceptor)
            }
            val okHttpClient = okHttpBuilder
                .cookieJar(OkHttpCookieJar())
                .build()
                //.addInterceptor(LoadCookiesInterceptor())
            val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}