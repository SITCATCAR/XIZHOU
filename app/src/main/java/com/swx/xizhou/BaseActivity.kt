package com.swx.xizhou

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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

    protected open fun initData(){}
    protected open fun initAction(){}


    protected fun enableInsetsView(view: View, top: Boolean,bottom: Boolean){

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            val chosedBottom=if(imeVisible) ime.bottom else systemBars.bottom

            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right
            )
            if (top)v.updatePadding(top = systemBars.top)
            if (bottom)v.updatePadding(bottom = chosedBottom)

            insets
        }
    }

}