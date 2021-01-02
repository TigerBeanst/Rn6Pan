package com.jakting.rn6pan.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jakting.rn6pan.fragment.TransDownloadFragment
import com.jakting.rn6pan.fragment.TransUploadFragment

class TransmissionAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TransDownloadFragment()
            else -> TransUploadFragment()
        }
    }
}