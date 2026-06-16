package com.swx.xizhou.pages.settingPage

import com.swx.xizhou.BaseFragment
import com.swx.xizhou.MainActivity
import com.swx.xizhou.databinding.ItemLanguageOptionBinding
import com.swx.xizhou.databinding.SettingsFragmentBinding
import com.swx.xizhou.util.LanguageManager
import com.swx.xizhou.util.LanguageOption

class SettingsFragment : BaseFragment<SettingsFragmentBinding>(SettingsFragmentBinding::inflate) {
    private val languageOptionViews = mutableMapOf<String, ItemLanguageOptionBinding>()

    override fun initView() {
        enableInsetsView(binding.settingsScroll, top = true, bottom = false)
        setupLanguageOptions()
        updateLanguageView()
    }

    override fun loadData() {
    }

    private fun setupLanguageOptions() {
        languageOptionViews.clear()
        binding.layoutLanguageOptions.removeAllViews()

        LanguageManager.languageOptions.forEach { option ->
            val optionBinding = ItemLanguageOptionBinding.inflate(layoutInflater, binding.layoutLanguageOptions, false)
            bindLanguageOption(optionBinding, option)
            languageOptionViews[option.code] = optionBinding
            binding.layoutLanguageOptions.addView(optionBinding.root)
        }
    }
    
    private fun bindLanguageOption(
        optionBinding: ItemLanguageOptionBinding,
        option: LanguageOption
    ) {
        optionBinding.tvLanguageName.setText(option.nameRes)
        optionBinding.tvLanguageNativeName.setText(option.nativeNameRes)
        optionBinding.tvLanguageTag.setText(option.tagRes)
        optionBinding.tvLanguageFlag.setText(option.flagRes)
        optionBinding.layoutLanguageOption.setOnClickListener {
            changeLanguage(option.code)
        }
    }

    private fun changeLanguage(language: String) {
        if (LanguageManager.getLanguage(requireContext()) == language) {
            return
        }
        LanguageManager.setLanguage(requireContext(), language)
        (requireActivity() as? MainActivity)?.openSettingAfterRecreate()
        requireActivity().recreate()
    }

    private fun updateLanguageView(language: String = LanguageManager.getLanguage(requireContext())) {
        val selectedCode = LanguageManager.getLanguageOption(language).code
        languageOptionViews.forEach { (code, optionBinding) ->
            val isSelected = code == selectedCode
            optionBinding.rbLanguage.isChecked = isSelected
            optionBinding.layoutLanguageOption.alpha = if (isSelected) 1f else 0.86f
        }

        binding.tvCurrentLanguage.text = getString(
            com.swx.xizhou.R.string.settings_current_language,
            LanguageManager.getLanguageName(requireContext(), selectedCode)
        )
    }
}
