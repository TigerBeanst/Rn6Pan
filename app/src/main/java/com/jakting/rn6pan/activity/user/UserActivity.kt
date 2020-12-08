package com.jakting.rn6pan.activity.user

import android.os.Bundle
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R

class UserActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.user_toolbar_title)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}