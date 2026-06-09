package com.swx.xizhou.pages.createPage

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.swx.xizhou.BaseFragment
import com.swx.xizhou.activity.CalenderCreateActivity
import com.swx.xizhou.activity.XCreateActivity
import com.swx.xizhou.activity.YoutubeCreateActivity
import com.swx.xizhou.R
import com.swx.xizhou.databinding.CreateFragmentBinding
import com.swx.xizhou.event.Event

class CreateFragment: BaseFragment<CreateFragmentBinding>(CreateFragmentBinding::inflate) {

    var list=mutableListOf<CreateItem>()
    //适配器点击事件，替代回调
    private val onCreateItemClickEvent = Event<CreateItemClickEvent>()

    override fun initView() {
        setupToolbar()
        enableInsetsView(binding.toolbar,true,false)
        binding.RCView.layoutManager= GridLayoutManager(context,3)

        binding.RCView.adapter= CreateAdapter(activity,list,onCreateItemClickEvent)
        onCreateItemClickEvent += ::onItemClick
    }

    override fun onDestroyView() {
        onCreateItemClickEvent -= ::onItemClick
        super.onDestroyView()
    }

    override fun loadData() {
        list.add(CreateItem(getString(R.string.create_type_x), CreateItemType.X))
        list.add(CreateItem(getString(R.string.create_type_youtube), CreateItemType.Youtube))
        list.add(CreateItem(getString(R.string.create_type_calendar), CreateItemType.Calender))
    }

    private fun onItemClick(event: CreateItemClickEvent) {
        when (event.item.type) {
            CreateItemType.Youtube -> {
                val intent = Intent(activity, YoutubeCreateActivity::class.java)
                startActivity(intent)
            }
            CreateItemType.Calender -> {
                val intent = Intent(activity, CalenderCreateActivity::class.java)
                startActivity(intent)
            }
            CreateItemType.X -> {
                val intent = Intent(activity, XCreateActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupToolbar(){
        binding.toolbar.title=getString(R.string.create_title)
        binding.toolbar.inflateMenu(R.menu.create_menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.create_menu, menu)
        binding.toolbar.title = getString(R.string.create_title)
        super.onCreateOptionsMenu(menu, inflater)
    }
}