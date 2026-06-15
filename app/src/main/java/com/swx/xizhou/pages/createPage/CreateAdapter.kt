package com.swx.xizhou.pages.createPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.swx.xizhou.R
import com.swx.xizhou.event.Event

class CreateAdapter(
    val context: FragmentActivity?,
    val itemList: List<CreateItem>,
    val onCreateItemClickEvent: Event<CreateItemClickEvent>
) : RecyclerView.Adapter<CreateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.create_rcv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val createItem = itemList[position]
        val definition = CreateTypes.getByCreateType(createItem.type)
        viewHolder.itemName.text = createItem.name
        viewHolder.itemImage.setImageResource(definition.iconRes)
        viewHolder.itemView.setOnClickListener {
            // 触发点击事件
            onCreateItemClickEvent.invoke(CreateItemClickEvent(position, createItem))
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.createName)
        val itemImage: AppCompatImageView = view.findViewById(R.id.createImg)
    }
}
