package com.swx.xizhou.pages.settingPage

import com.swx.xizhou.BaseFragment
import com.swx.xizhou.MainActivity
import com.swx.xizhou.R
import com.swx.xizhou.databinding.ItemLanguageOptionBinding
import com.swx.xizhou.databinding.SettingsFragmentBinding
import com.swx.xizhou.util.LanguageManager
import com.swx.xizhou.util.LanguageOption
import com.swx.xizhou.util.SPUtil

class SettingsFragment : BaseFragment<SettingsFragmentBinding>(SettingsFragmentBinding::inflate) {
    private val languageOptionViews = mutableMapOf<String, ItemLanguageOptionBinding>()

    override fun initView() {
        enableInsetsView(binding.settingsScroll, top = true, bottom = false)
        setupBeepOption()
        setupLanguageOptions()
        updateLanguageView()
    }

    override fun loadData() {
    }

    private fun setupBeepOption() {
        val isBeepEnabled = SPUtil.getBoolean(
            SPUtil.KEY_BEEP_ENABLED,
            SPUtil.DEFAULT_BEEP_ENABLED,
            requireContext()
        )
        binding.scBeep.isChecked = isBeepEnabled
        updateBeepStatus(isBeepEnabled)

        binding.scBeep.setOnCheckedChangeListener { _, isChecked ->
            SPUtil.set(SPUtil.KEY_BEEP_ENABLED, isChecked, requireContext())
            updateBeepStatus(isChecked)
        }

        binding.layoutBeepOption.setOnClickListener {
            binding.scBeep.isChecked = !binding.scBeep.isChecked
        }
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

    private fun updateBeepStatus(isEnabled: Boolean) {
        binding.tvBeepStatus.setText(
            if (isEnabled) {
                R.string.settings_beep_enabled
            } else {
                R.string.settings_beep_disabled
            }
        )
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
