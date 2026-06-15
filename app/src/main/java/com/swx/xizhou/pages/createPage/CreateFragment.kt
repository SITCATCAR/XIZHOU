package com.swx.xizhou.pages.createPage

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.swx.xizhou.BaseFragment
import com.swx.xizhou.R
import com.swx.xizhou.databinding.CreateFragmentBinding
import com.swx.xizhou.event.Event

class CreateFragment : BaseFragment<CreateFragmentBinding>(CreateFragmentBinding::inflate) {

    var list = mutableListOf<CreateItem>()
    // 适配器点击事件，替代回调
    private val onCreateItemClickEvent = Event<CreateItemClickEvent>()

    override fun initView() {
        setupToolbar()
        enableInsetsView(binding.toolbar, true, false)
        binding.RCView.layoutManager = GridLayoutManager(context, 3)
        binding.RCView.adapter = CreateAdapter(activity, list, onCreateItemClickEvent)
        onCreateItemClickEvent += ::onItemClick
    }

    override fun onDestroyView() {
        onCreateItemClickEvent -= ::onItemClick
        super.onDestroyView()
    }

    override fun loadData() {
        list.clear()
        CreateTypes.all.forEach { definition ->
            list.add(CreateItem(getString(definition.nameRes), definition.type))
        }
    }

    private fun onItemClick(event: CreateItemClickEvent) {
        val definition = CreateTypes.getByCreateType(event.item.type)
        val intent = Intent(activity, definition.activityClass)
        startActivity(intent)
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.create_title)
        binding.toolbar.inflateMenu(R.menu.create_menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.create_menu, menu)
        binding.toolbar.title = getString(R.string.create_title)
        super.onCreateOptionsMenu(menu, inflater)
    }
}
