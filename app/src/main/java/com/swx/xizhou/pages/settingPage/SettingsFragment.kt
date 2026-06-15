package com.swx.xizhou.pages.settingPage

import com.swx.xizhou.BaseFragment
import com.swx.xizhou.MainActivity
import com.swx.xizhou.databinding.SettingsFragmentBinding
import com.swx.xizhou.util.LanguageManager

class SettingsFragment : BaseFragment<SettingsFragmentBinding>(SettingsFragmentBinding::inflate) {

    override fun initView() {
        enableInsetsView(binding.toolbar, top = true, bottom = false)
        updateLanguageView()

        binding.layoutLanguageSystem.setOnClickListener {
            changeLanguage(LanguageManager.LANGUAGE_SYSTEM)
        }

        binding.layoutLanguageEnglish.setOnClickListener {
            changeLanguage(LanguageManager.LANGUAGE_ENGLISH)
        }

        binding.layoutLanguageChinese.setOnClickListener {
            changeLanguage(LanguageManager.LANGUAGE_CHINESE)
        }
    }

    override fun loadData() {
    }

    private fun changeLanguage(language: String) {
        if (LanguageManager.getLanguage(requireContext()) == language) {
            return
        }
        LanguageManager.setLanguage(requireContext(), language)
        updateLanguageView(language)
        (requireActivity() as? MainActivity)?.openSettingAfterRecreate()
        requireActivity().recreate()
    }

    private fun updateLanguageView(language: String = LanguageManager.getLanguage(requireContext())) {
        binding.rbLanguageSystem.isChecked = language == LanguageManager.LANGUAGE_SYSTEM
        binding.rbLanguageEnglish.isChecked = language == LanguageManager.LANGUAGE_ENGLISH
        binding.rbLanguageChinese.isChecked = language == LanguageManager.LANGUAGE_CHINESE
        binding.tvCurrentLanguage.text = getString(
            com.swx.xizhou.R.string.settings_current_language,
            LanguageManager.getLanguageName(requireContext(), language)
        )
    }
}
