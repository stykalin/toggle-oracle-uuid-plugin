package com.github.stykalin.tou.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class ToggleOracleUuidAppSettingsConfigurable : Configurable {

    private val settings = ToggleOracleUuidAppSettingsState.getInstance()

    private val panel: DialogPanel = panel {
        row {
            checkBox("Convert RAW to UPPERCASE UUID")
                .bindSelected(settings::useUpperCase)
        }
    }

    override fun getDisplayName(): String = "Toggle Oracle Uuid"

    override fun createComponent(): JComponent = panel

    override fun isModified(): Boolean = panel.isModified()

    override fun apply() = panel.apply()
}