package com.jakting.rn6pan.utils

data class createDestination(
    val destination: String,
    val expireTime: Long
)

data class checkDestination(
    val status: Int,
    val token: String,
    val state: String
)

data class UserInfo(
    val identity: Long,
    val name: String,
    val password: String,
    val salt: String,
    val countryCode: Int,
    val phone: String,
    val email: String,
    val createTime: Long,
    val createAddr: String,
    val icon: String,
    val spaceUsed: Long,
    val spaceCapacity: Long,
    val type: Int,
    val status: Int,
    val version: Int,
    val vip: Int,
    val vipExpireTime: Long,
    val lastActivateTime: Long
)

data class OfflineQuota(
    var dailyUsed:Int,
    var dailyQuota:Int,
    var available:Int
)