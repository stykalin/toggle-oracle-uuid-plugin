package com.github.stykalin.tou.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.github.stykalin.tou.settings.ToggleOracleUuidAppSettingsState",
    storages = [Storage("ToggleOracleUuidAppSettingsState.xml")]
)
@Service(Service.Level.APP)
class ToggleOracleUuidAppSettingsState(
    var useUpperCase: Boolean = false,
    var useRaw16Mode: Boolean = false,
) : PersistentStateComponent<ToggleOracleUuidAppSettingsState> {
    override fun getState(): ToggleOracleUuidAppSettingsState = this

    override fun loadState(state: ToggleOracleUuidAppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): ToggleOracleUuidAppSettingsState =
            ApplicationManager.getApplication().getService(ToggleOracleUuidAppSettingsState::class.java)
    }
}