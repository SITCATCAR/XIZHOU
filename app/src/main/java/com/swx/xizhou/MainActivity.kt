package com.swx.xizhou

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.swx.xizhou.databinding.ActivityMainBinding
import com.swx.xizhou.pages.createPage.CreateFragment
import com.swx.xizhou.pages.historyPage.HistoryFragment
import com.swx.xizhou.pages.scanPage.ScanFragment
import com.swx.xizhou.pages.settingPage.SettingsFragment

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private lateinit var createFragment: CreateFragment
    private lateinit var historyFragment: HistoryFragment
    private lateinit var scanFragment: ScanFragment
    private lateinit var settingFragment: SettingsFragment

    companion object {
        private const val EXTRA_OPEN_SETTING = "extra_open_setting"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        createFragment = CreateFragment()
        historyFragment = HistoryFragment()
        scanFragment = ScanFragment()
        settingFragment = SettingsFragment()
        initFragment()
        enableInsetsView(binding.layoutBottomBar.root, false, true)
    }

    override fun initAction() {
        super.initAction()

        binding.layoutBottomBar.bottomCreate.setOnClickListener {
            scanFragment.closeCamera()
            showFragment(createFragment)
        }

        binding.layoutBottomBar.bottomHistory.setOnClickListener {
            scanFragment.closeCamera()
            showFragment(historyFragment)
        }

        binding.layoutBottomBar.bottomScan.setOnClickListener {
            scanFragment.openCamera()
            showFragment(scanFragment)
        }

        binding.layoutBottomBar.bottomSetting.setOnClickListener {
            scanFragment.closeCamera()
            showFragment(settingFragment)
        }
    }

    fun openSettingAfterRecreate() {
        intent.putExtra(EXTRA_OPEN_SETTING, true)
    }

    private fun initFragment() {
        val shouldOpenSetting = intent.getBooleanExtra(EXTRA_OPEN_SETTING, false)
        intent.putExtra(EXTRA_OPEN_SETTING, false)

        removeRestoredFragments()

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(false)
            .add(R.id.m_fragment_holder, createFragment)
            .add(R.id.m_fragment_holder, historyFragment)
            .add(R.id.m_fragment_holder, scanFragment)
            .add(R.id.m_fragment_holder, settingFragment)
            .hide(createFragment)
            .hide(historyFragment)
            .hide(if (shouldOpenSetting) scanFragment else settingFragment)
            .commitNow()

        syncFragmentViewVisibility(if (shouldOpenSetting) settingFragment else scanFragment)
    }

    private fun removeRestoredFragments() {
        val restoredFragments = supportFragmentManager.fragments
        if (restoredFragments.isEmpty()) {
            return
        }

        val transaction = supportFragmentManager.beginTransaction()
            .setReorderingAllowed(false)
        restoredFragments.forEach { fragment ->
            transaction.remove(fragment)
        }
        transaction.commitNowAllowingStateLoss()
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(false)
            .hide(createFragment)
            .hide(historyFragment)
            .hide(scanFragment)
            .hide(settingFragment)
            .show(fragment)
            .commitNowAllowingStateLoss()

        syncFragmentViewVisibility(fragment)
    }

    private fun syncFragmentViewVisibility(visibleFragment: Fragment) {
        listOf(createFragment, historyFragment, scanFragment, settingFragment).forEach { fragment ->
            fragment.view?.visibility = if (fragment == visibleFragment) View.VISIBLE else View.GONE
        }
    }
}
