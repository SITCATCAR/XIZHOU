package com.swx.xizhou.pages.historyPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryItemVO
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.pages.historyPage.statemachine.HistorySelectionState

class HistoryAdapter(
    val context: FragmentActivity?,
    val list: List<HistoryItemVO>,
    private val fragment: HistoryFragment
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.history_rcv_item, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, index: Int) {
        val item = list[index]
        viewHolder.tvTop.text = item.contentTop
        viewHolder.tvBottom.text = item.contentBottom
        setFormatIcon(viewHolder.ivLeft, item.type)
        setupRightIcon(viewHolder.ivRight, item)
        viewHolder.itemView.setOnClickListener { fragment.onItemClick(item.id, index) }
        viewHolder.itemView.setOnLongClickListener {
            fragment.onItemLongPress(item.id, index)
            true
        }
    }

    override fun getItemCount(): Int = list.size

    private fun setFormatIcon(ivLeft: AppCompatImageView, type: HistoryType) {
        val iconRes = when (type) {
            HistoryType.YOUTUBE -> R.drawable.vector_ic_youtube
            HistoryType.CALENDAR -> R.drawable.vector_ic_calendar
            HistoryType.X->R.drawable.vector_ic_x
            else -> R.drawable.parse_format_icon_text
        }
        ivLeft.setImageResource(iconRes)
    }

    private fun setupRightIcon(ivRight: AppCompatImageView, item: HistoryItemVO) {
        if (fragment.stateMachine.currentState is HistorySelectionState) {
            val checkboxRes = if (fragment.isItemSelected(item.id)) {
                R.drawable.ic_checkbox_checked
            } else {
                R.drawable.ic_checkbox_unchecked
            }
            ivRight.setImageResource(checkboxRes)
        } else {
            ivRight.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLeft = view.findViewById<AppCompatImageView>(R.id.ivLeft)
        val ivRight = view.findViewById<AppCompatImageView>(R.id.ivRight)
        val tvTop = view.findViewById<TextView>(R.id.tvTop)
        val tvBottom = view.findViewById<TextView>(R.id.tvBottom)
    }
}