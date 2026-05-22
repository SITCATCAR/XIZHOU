package com.swx.xizhou

import android.os.Bundle
import com.swx.xizhou.databinding.ActivityMainBinding
import com.swx.xizhou.pages.createPage.CreateFragment
import com.swx.xizhou.pages.historyPage.HistoryFragment
import com.swx.xizhou.pages.scanPage.ScanFragment

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private lateinit var createFragment: CreateFragment
    private lateinit var historyFragment: HistoryFragment
    private lateinit var scanFragment: ScanFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        createFragment= CreateFragment()
        historyFragment= HistoryFragment()
        scanFragment= ScanFragment()
        //使用replace fragment会崩溃
        initFragment()
    }

    override fun initAction() {
        super.initAction()

        //目前需手动关闭/开启相机
        binding.layoutBottomBar.bottomCreate.setOnClickListener {
            scanFragment.closeCamera()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.hide(historyFragment).hide(scanFragment).show(createFragment).commit()
        }

        binding.layoutBottomBar.bottomHistory.setOnClickListener {
            scanFragment.closeCamera()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.hide(createFragment).hide(scanFragment).show(historyFragment).commit()
        }
        binding.layoutBottomBar.bottomScan.setOnClickListener {
            scanFragment.openCamera()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.hide(createFragment).hide(historyFragment).show(scanFragment).commit()
        }
    }
    private fun initFragment(){
        supportFragmentManager.beginTransaction().add(R.id.m_fragment_holder,createFragment)
            .add(R.id.m_fragment_holder,historyFragment).add(R.id.m_fragment_holder, scanFragment)
            .hide(createFragment).hide(historyFragment).commit()
    }
}