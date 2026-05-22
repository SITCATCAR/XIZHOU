package com.swx.xizhou.pages.historyPage.statemachine

import com.swx.xizhou.R
import com.swx.xizhou.pages.historyPage.HistoryFragment

class HistorySelectionState : HistoryState() {

    override fun enter(fragment: HistoryFragment) {
        updateTitle(fragment)
        fragment.refreshAdapter()
        fragment.updateMenu()
    }

    override fun exit(fragment: HistoryFragment) {
        fragment.clearSelection()
        fragment.refreshAdapter()
        fragment.updateMenu()
    }

    fun updateTitle(fragment: HistoryFragment) {
        val count = fragment.selectedIds.size
        fragment.toolbar.title = if (count == 0) {
            fragment.getString(R.string.selection_title)
        } else {
            fragment.getString(R.string.selected_count, count)
        }
    }
}