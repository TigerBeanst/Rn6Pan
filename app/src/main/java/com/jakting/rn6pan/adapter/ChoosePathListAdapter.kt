package com.jakting.rn6pan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jakting.rn6pan.R
import com.jakting.rn6pan.activity.common.ChoosePathActivity
import com.jakting.rn6pan.api.data.FileOrDirectory
import com.jakting.rn6pan.utils.MyApplication
import com.jakting.rn6pan.utils.MyApplication.Companion.appContext
import com.jakting.rn6pan.utils.getPrintSize
import kotlinx.android.synthetic.main.activity_choose_path.*
import java.text.SimpleDateFormat


class ChoosePathListAdapter(
    private val directoryList: List<FileOrDirectory>,
    private val activity: ChoosePathActivity
) :
    RecyclerView.Adapter<ChoosePathListAdapter.ViewHolder>() {
    lateinit var parentContext: Context

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val directoryLayout: LinearLayout = view.findViewById(R.id.choose_path_layout)
        val directoryIcon: ImageView = view.findViewById(R.id.choose_path_fileicon)
        val directoryName: TextView = view.findViewById(R.id.choose_path_filename)
        val directoryInfo: TextView = view.findViewById(R.id.choose_path_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        parentContext = parent.context
        val view =
            LayoutInflater.from(parentContext)
                .inflate(R.layout.item_choose_path, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val directory = directoryList[position]
        holder.directoryIcon.setImageDrawable(
            ContextCompat.getDrawable(appContext, R.drawable.file_icon_directory)
        )
        holder.directoryName.apply {
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint = directory.name
        }
        val params = holder.directoryInfo.layoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.directoryInfo.apply {
            layoutParams = params
            setBackgroundColor(ContextCompat.getColor(parentContext, android.R.color.transparent))
            hint =
                getPrintSize(directory.size) + "   " + SimpleDateFormat("YYYY/MM/dd").format(
                    directory.ctime
                )
        }
        holder.directoryLayout.setOnClickListener{
            MyApplication.parentChoosePathList.add(directory.path)
            (parentContext as ChoosePathActivity).nowOnPage = 0
            (parentContext as ChoosePathActivity).choose_path_swipeLayout.autoRefresh()
        }
    }

    override fun getItemCount() = directoryList.size

}