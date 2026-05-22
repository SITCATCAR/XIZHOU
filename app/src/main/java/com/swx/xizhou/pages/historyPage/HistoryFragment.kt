package com.swx.xizhou.pages.historyPage

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.swx.xizhou.BaseFragment
import com.swx.xizhou.R
import com.swx.xizhou.activity.HistoryDisplayActivity
import com.swx.xizhou.database.HistoryDBHelper
import com.swx.xizhou.databinding.HistoryFragmentBinding
import com.swx.xizhou.pages.historyPage.statemachine.HistoryNormalState
import com.swx.xizhou.pages.historyPage.statemachine.HistorySelectionState
import com.swx.xizhou.pages.historyPage.statemachine.HistoryStateMachine

class HistoryFragment :
    BaseFragment<HistoryFragmentBinding>(HistoryFragmentBinding::inflate) {

    lateinit var stateMachine: HistoryStateMachine
        private set

    val toolbar get() = binding.toolbar

    private val pagerFragments = mutableListOf<HistoryPagerFragment>()

    val selectedIds = mutableSetOf<Long>()

    override fun initView() {
        binding.pager.adapter = HistoryPagerAdapter(this)
        bindTabLayout(binding.tabLayout, binding.pager)
        setupToolbar()
        stateMachine = HistoryStateMachine(this)
        stateMachine.changeState(HistoryNormalState())

        //切换pager时转到普通状态
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                stateMachine.changeState(HistoryNormalState())
            }
        })
    }

    override fun loadData() {}

    private fun setupToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
    }


    fun onItemClick(itemId: Long, position: Int) {
        when (stateMachine.currentState) {
            is HistoryNormalState -> openDetail(itemId)
            is HistorySelectionState -> {
                toggleSelection(itemId)
                refreshSelectionTitle()
            }
            else -> {

            }
        }
    }

    /**
     * 长按弹修改框
     */
    fun onItemLongPress(itemId: Long, position: Int) {
        getCurrentPagerFragment()?.showEditTitleDialog(itemId, position)
    }

    private fun getCurrentPagerFragment(): HistoryPagerFragment? {
        return pagerFragments.getOrNull(binding.pager.currentItem)
    }


    fun toggleSelection(itemId: Long) {
        if (selectedIds.contains(itemId)) {
            selectedIds.remove(itemId)
        } else {
            selectedIds.add(itemId)
        }
        refreshAdapter()
    }


    fun clearSelection() {
        selectedIds.clear()
    }


    fun isItemSelected(id: Long): Boolean {
        return selectedIds.contains(id)
    }


    private fun refreshSelectionTitle() {
        (stateMachine.currentState as? HistorySelectionState)
            ?.updateTitle(this)
        updateMenu()
    }


    fun refreshAdapter() {
        pagerFragments.forEach { it.refreshAdapter() }
    }

    fun updateMenu() {
        activity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.history_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val isSelection = stateMachine.currentState is HistorySelectionState
        menu.findItem(R.id.action_select_mode)?.isVisible = !isSelection
        menu.findItem(R.id.action_exit_selection)?.isVisible = isSelection
        menu.findItem(R.id.action_select_all)?.isVisible = isSelection
        menu.findItem(R.id.action_delete)?.isVisible = isSelection && selectedIds.isNotEmpty()
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_select_mode -> stateMachine.changeState(HistorySelectionState())
            R.id.action_exit_selection -> stateMachine.changeState(HistoryNormalState())
            R.id.action_select_all -> selectAll()
            R.id.action_delete -> deleteSelectedItems()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * 全选/取消 当前界面全选
     */
    private fun selectAll() {
        val ids = mutableSetOf<Long>()
//        pagerFragments.forEach { ids.addAll(it.getAllItemIds()) }
        //只选择当前界面的全部
        val allItemIds = pagerFragments[binding.pager.currentItem].getAllItemIds()
        ids.addAll(allItemIds)
        if (selectedIds.size == ids.size) {
            // 已经全选，取消全选
            selectedIds.clear()
        } else {
            // 未全选
            selectedIds.clear()
            selectedIds.addAll(ids)
        }

        refreshSelectionTitle()
        refreshAdapter()
    }

    /**
     * 删除选中项
     */
    private fun deleteSelectedItems() {
        if (selectedIds.isEmpty()) return
        val message = getString(R.string.delete_confirm_message, selectedIds.size)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_confirm)
            .setMessage(message)
            .setPositiveButton(R.string.delete) { _, _ ->
                pagerFragments.forEach { it.deleteItems(selectedIds) }
                stateMachine.changeState(HistoryNormalState())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }


    private fun openDetail(itemId: Long) {
        val tableName = getCurrentTableNamePublic()
        val intent = Intent(requireContext(), HistoryDisplayActivity::class.java).apply {
            putExtra(HistoryDisplayActivity.EXTRA_ITEM_ID, itemId)
            putExtra(HistoryDisplayActivity.EXTRA_TABLE_NAME, tableName)
        }
        startActivity(intent)
    }

    fun registerPagerFragment(fragment: HistoryPagerFragment) {
        if (!pagerFragments.contains(fragment)) {
            pagerFragments.add(fragment)
        }
    }

    fun getCurrentTableNamePublic(): String {
        return when (binding.pager.currentItem) {
            0 -> HistoryDBHelper.S_TABLE_NAME
            1 -> HistoryDBHelper.C_TABLE_NAME
            else -> HistoryDBHelper.C_TABLE_NAME
        }
    }

    private fun bindTabLayout(tab: TabLayout, pager: ViewPager2) {
        TabLayoutMediator(tab, pager) { tabItem, position ->
            when (position) {
                0 -> tabItem.text = getString(R.string.tab_scan)
                1 -> tabItem.text = getString(R.string.tab_create)
            }
        }.attach()
    }


    override fun onDestroyView() {
        pagerFragments.clear()
        super.onDestroyView()
    }
}