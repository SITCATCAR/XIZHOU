package com.swx.xizhou.pages.historyPage.statemachine

import com.swx.xizhou.R
import com.swx.xizhou.pages.historyPage.HistoryFragment


class HistoryNormalState : HistoryState() {

    override fun enter(fragment: HistoryFragment) {
        fragment.clearSelection()
        fragment.toolbar.title = fragment.getString(R.string.history)
        fragment.refreshAdapter()
        fragment.updateMenu()
    }
}