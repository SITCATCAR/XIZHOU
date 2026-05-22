package com.swx.xizhou.pages.historyPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HistoryPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {


    override fun createFragment(p0: Int): Fragment {
        val pagerFragment = HistoryPagerFragment()
        pagerFragment.arguments= Bundle().apply {
            putInt("TYPE",p0)
        }

        return pagerFragment
    }

    override fun getItemCount(): Int {
       return 2
    }
}