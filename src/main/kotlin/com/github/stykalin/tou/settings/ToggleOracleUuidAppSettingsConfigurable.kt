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
            comment(
                "By default, the plugin performs a direct conversion without byte swapping: <br>" +
                        "<code>DD6575AFC6B6A149840E6008432D7D92 -> dd6575af-c6b6-a149-840e-6008432d7d92</code>.<br><br>" +
                        "If you need to convert with byte swapping for Oracle RAW(16):<br>" +
                        "<code>AF7565DDB6C649A1840E6008432D7D92 -> DD6575AF-C6B6-A149-840E-6008432D7D92<code>,<br>" +
                        "you should check the <code>\"Enable RAW(16) mode\"</code> checkbox."
            )
        }

        row {
            checkBox("Enable RAW(16) mode")
                .bindSelected(settings::useRaw16Mode)
        }
        row {
            checkBox("Convert RAW to UPPERCASE UUID")
                .bindSelected(settings::useUpperCase)
            contextHelp(
                "Option enables UPPER CASE format for UUID type string:<br>" +
                        "<code>false: dd6575af-c6b6-a149-840e-6008432d7d92</code><br>" +
                        "<code>true:  DD6575AF-C6B6-A149-840E-6008432D7D92<code>"
            )
        }
    }

    override fun getDisplayName(): String = "Toggle Oracle Uuid"

    override fun createComponent(): JComponent = panel

    override fun isModified(): Boolean = panel.isModified()

    override fun apply() = panel.apply()
}