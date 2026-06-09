package com.swx.xizhou

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<vb: ViewBinding>(val infate:(LayoutInflater, ViewGroup?, Boolean)->vb): Fragment() {

    private var _binding:vb?=null
    val binding get() = _binding!!
    protected val isBindingAvailable: Boolean get() = _binding != null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         _binding = infate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

    abstract fun initView()
    abstract fun loadData()

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