package com.swx.xizhou.pages.historyPage

import android.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.swx.xizhou.BaseFragment
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryDBHelper
import com.swx.xizhou.database.HistoryItemVO
import com.swx.xizhou.database.HistoryMapper
import com.swx.xizhou.databinding.HistoryPagerFragmentBinding
import com.swx.xizhou.event.Event
import com.swx.xizhou.pages.historyPage.statemachine.HistoryNormalState

class HistoryPagerFragment :
    BaseFragment<HistoryPagerFragmentBinding>(HistoryPagerFragmentBinding::inflate) {

    companion object {
        var onItemChangeEvent = Event<Unit>()
    }

    private lateinit var historyMapper: HistoryMapper
    private lateinit var adapter: HistoryAdapter

    val listScan = mutableListOf<HistoryItemVO>()
    val listCreate = mutableListOf<HistoryItemVO>()

    override fun initView() {
        binding.historyRCY.layoutManager = GridLayoutManager(activity, 1)
        chooseHistoryData()
        (parentFragment as? HistoryFragment)?.registerPagerFragment(this)
        onItemChangeEvent += ::onItemChange
    }
    //刷新数据，外部调用
    private fun onItemChange(unit: Unit) {
        reSelectData(Unit)
    }

    override fun loadData() {
        historyMapper = HistoryMapper(activity)
        if(!historyMapper.hasHistory(getCurrentTableName()))
            Toast.makeText(activity,getString(R.string.has_no_data), Toast.LENGTH_LONG).show()

        selectFromDB()
    }

    private fun chooseHistoryData() {
        arguments?.takeIf { it.containsKey("TYPE") }?.apply {
            when (getInt("TYPE")) {
                0 -> {
                    adapter = HistoryAdapter(activity, listScan, parentFragment as HistoryFragment)
                    binding.historyRCY.adapter = adapter
                }
                1 -> {
                    adapter = HistoryAdapter(activity, listCreate, parentFragment as HistoryFragment)
                    binding.historyRCY.adapter = adapter
                }
            }
        }
    }

    fun refreshAdapter() {
        adapter.notifyDataSetChanged()
    }

    fun containsItem(itemId: Long): Boolean {
        val currentList = when (arguments?.getInt("TYPE")) {
            0 -> listScan
            1 -> listCreate
            else -> return false
        }
        return currentList.any { it.id == itemId }
    }

    fun showEditTitleDialog(itemId: Long, position: Int) {
        val currentList = when (arguments?.getInt("TYPE")) {
            0 -> listScan
            1 -> listCreate
            else -> return
        }
        val item = currentList.find { it.id == itemId } ?: return
        val tableName = getCurrentTableName()

        val input = EditText(activity).apply {
            setText(item.contentTop)
            setSelection(text.length)
            hint = getString(R.string.hint_new_title)
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.edit_title)
            .setView(input)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val newTitle = input.text.toString()
                if (newTitle.isNotEmpty()) {
                    val newTimestamp = System.currentTimeMillis()
                    historyMapper.updateTitle(item.id, newTitle, newTimestamp, tableName)
                    reSelectData(Unit)
                    Toast.makeText(activity, R.string.title_updated, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, R.string.title_empty_error, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun deleteItems(ids: Set<Long>) {
        val currentList = when (arguments?.getInt("TYPE")) {
            0 -> listScan
            1 -> listCreate
            else -> return
        }
        val tableName = getCurrentTableName()
        val relevantIds = ids.filter { id -> currentList.any { it.id == id } }.toSet()
        if (relevantIds.isEmpty()) return
        historyMapper.deleteByIds(relevantIds, tableName)
        reSelectData(Unit)
        Toast.makeText(activity, getString(R.string.delete_completed, relevantIds.size), Toast.LENGTH_SHORT).show()
    }

    /**
     * 获取所有Item ID
     */
    fun getAllItemIds(): Set<Long> {
        return when (arguments?.getInt("TYPE")) {
            0 -> listScan.map { it.id }.toSet()
            1 -> listCreate.map { it.id }.toSet()
            else -> emptySet()
        }
    }

    private fun getCurrentTableName(): String {
        return when (arguments?.getInt("TYPE")) {
            0 -> HistoryDBHelper.S_TABLE_NAME
            1 -> HistoryDBHelper.C_TABLE_NAME
            else -> HistoryDBHelper.C_TABLE_NAME
        }
    }

    private fun reSelectData(value: Unit) {
        listCreate.clear()
        listScan.clear()
        selectFromDB()
        adapter.notifyDataSetChanged()
    }

    private fun selectFromDB() {
        val citemDTOS = historyMapper.selectAll(HistoryDBHelper.C_TABLE_NAME)
        for (dto in citemDTOS) {
            val vo = HistoryItemVO(
                type = dto.format,
                contentTop = dto.title,
                contentBottom = dto.format.toString(),
                timestamp = dto.timestamp,
                id = dto.id
            )
            listCreate.add(vo)
        }
        val sitemDTOS = historyMapper.selectAll(HistoryDBHelper.S_TABLE_NAME)
        for (dto in sitemDTOS) {
            val vo = HistoryItemVO(
                type = dto.format,
                contentTop = dto.title,
                contentBottom = dto.format.toString(),
                timestamp = dto.timestamp,
                id = dto.id
            )
            listScan.add(vo)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            val parentFragment = parentFragment as? HistoryFragment ?: return
            parentFragment.stateMachine.changeState(HistoryNormalState())
        }
    }

    override fun onDestroyView() {
        onItemChangeEvent -= ::onItemChange
        super.onDestroyView()
    }


}