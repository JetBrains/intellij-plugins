package com.intellij.plugins.serialmonitor

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.State.NameGetter
import com.intellij.openapi.components.Storage
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Service
@State(name = "SerialPortProfiles",
       storages = [Storage(value = "serial-port-profiles.xml", roamingType = RoamingType.PER_OS)],
       category = SettingsCategory.TOOLS,
       presentableName = SerialProfileService.PresentableName::class)
class SerialProfileService(val cs: CoroutineScope) : PersistentStateComponent<SerialProfilesState> {
  private var myState = MutableStateFlow(SerialProfilesState())
  val profilesFlow: Flow<Map<String, SerialPortProfile>> = myState.map { it.profiles }.distinctUntilChanged()

  override fun getState(): SerialProfilesState {
    return myState.value
  }

  override fun loadState(state: SerialProfilesState) {
    myState.value = state
  }

  fun setDefaultProfile(defaultProfile: SerialPortProfile) {
    myState.value = state.copy(defaultProfile=defaultProfile)
  }

  fun setProfiles(profiles: MutableMap<String, SerialPortProfile>) {
    myState.value = state.copy(profiles=profiles)
  }

  fun getProfiles(): Map<String, SerialPortProfile> = myState.value.profiles

  fun copyDefaultProfile(portName: @NlsSafe String? = null): SerialPortProfile {
    val profile = myState.value.defaultProfile.copy()
    if(portName!=null) profile.portName = portName
    return profile
  }

  fun defaultBaudRate(): Int = myState.value.defaultProfile.baudRate

  enum class NewLine(private val displayKey: String, val value: String) {
    CR("uart.newline.cr", "\r"),
    LF("uart.newline.lf", "\n"),
    CRLF("uart.newline.crlf", "\r\n");

    override fun toString(): String = SerialMonitorBundle.message(displayKey)
  }

  class PresentableName : NameGetter() {
    override fun get(): String = SerialMonitorBundle.message("presentable.name")
  }

}

