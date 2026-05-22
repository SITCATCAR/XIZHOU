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
): RecyclerView.Adapter<CreateAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.create_rcv_item, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int
    ) {
        val createItem = itemList[position]
        viewHolder.itemName.text=createItem.name
        when(createItem.type){
            CreateItemType.X->
                viewHolder.itemImage.setImageResource(R.drawable.vector_ic_x)

            CreateItemType.Youtube->
                viewHolder.itemImage.setImageResource(R.drawable.vector_ic_youtube)

            CreateItemType.Calender->
                viewHolder.itemImage.setImageResource(R.drawable.vector_ic_calendar)
        }

        viewHolder.itemView.setOnClickListener {
            //onclicklistener.onCreateItemClick(position,createItem)
            //触发点击事件
            onCreateItemClickEvent.invoke(CreateItemClickEvent(position, createItem))
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val itemName: TextView=view.findViewById(R.id.createName)
        val itemImage: AppCompatImageView=view.findViewById(R.id.createImg)
    }
}