package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.user.offline.OfflineByLinksActivity
import com.jakting.rn6pan.api.data.OfflineParse
import com.jakting.rn6pan.utils.getPrintSize


class OfflineByLinksListAdapter(
    private val offlineParseList: List<OfflineParse>,
    private val activity: OfflineByLinksActivity
) :
    RecyclerView.Adapter<OfflineByLinksListAdapter.ViewHolder>() {
    lateinit var parentContext: Context

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val offlineByLinksLayout: LinearLayout =
            view.findViewById(R.id.offline_by_linkslist_layout)
        val offlineByLinksName: TextView = view.findViewById(R.id.offline_by_linkslist_filename)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        parentContext = parent.context
        val view =
            LayoutInflater.from(parentContext)
                .inflate(R.layout.item_offline_parse, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offlineParse = offlineParseList[position]
        holder.offlineByLinksName.hint =
            "(" + getPrintSize(offlineParse.info.size) + ")" + offlineParse.info.name
        holder.offlineByLinksLayout.setOnLongClickListener {
            MaterialAlertDialogBuilder(parentContext)
                .setMessage(parentContext.getString(R.string.offline_new_by_links_task_delete_title))
                .setPositiveButton(parentContext.getString(R.string.ok)) { _, _ ->
                    (parentContext as OfflineByLinksActivity).nowParseList.removeAt(position)
                    (parentContext as OfflineByLinksActivity).offlineByLinksListAdapter.notifyDataSetChanged()
                }
                .setNegativeButton(parentContext.getString(R.string.cancel)){_,_->}
                .show()
            true
        }
    }

    override fun getItemCount() = offlineParseList.size


}