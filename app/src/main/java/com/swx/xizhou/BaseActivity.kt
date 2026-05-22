package com.swx.xizhou

import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity <vb: ViewBinding>(val inflate:(LayoutInflater)->vb) : AppCompatActivity() {

    protected lateinit var binding: vb
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
        initAction()
    }

    protected abstract fun initView()

    open fun initData(){}
    open fun initAction(){}

}