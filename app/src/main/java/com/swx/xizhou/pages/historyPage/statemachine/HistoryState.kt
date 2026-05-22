package com.swx.xizhou.pages.historyPage.statemachine

import com.swx.xizhou.pages.historyPage.HistoryFragment


abstract class HistoryState {

    open fun enter(fragment: HistoryFragment) {}


    open fun exit(fragment: HistoryFragment) {}
}