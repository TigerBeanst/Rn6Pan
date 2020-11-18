package com.jakting.rn6pan.utils

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import com.jakting.rn6pan.R
import com.jakting.rn6pan.user.FileListActivity

interface ItemListener {
    fun onLongClick(v: View?): Boolean
}

class Presenter(var context: Context) : ActionMode.Callback, ItemListener {


    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        val menuInflater = MenuInflater(context)
        menuInflater.inflate(R.menu.menu_file_list_multi, menu)
        (context as FileListActivity).adapter.startActionMode()
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.delete -> mAdapter.deleteItems()
//            R.id.select_all -> mAdapter.selectAll()
            else -> {
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        (context as FileListActivity).adapter.stopActionMode()
    }

    override fun onLongClick(v: View?): Boolean {
        (context as FileListActivity).startSupportActionMode(this)
        (context as FileListActivity).adapter.postionLongPress =
        return true
    }
}