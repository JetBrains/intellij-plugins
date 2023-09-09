package com.intellij.plugins.serialmonitor

import com.intellij.openapi.components.*
import com.intellij.openapi.components.State.NameGetter
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle

@Service
@State(name = "SerialPortProfiles",
       storages = [Storage(value = "serial-port-profiles.xml", roamingType = RoamingType.PER_OS)],
       category = SettingsCategory.TOOLS,
       presentableName = SerialProfileService.PresentableName::class)
class SerialProfileService : PersistentStateComponent<SerialProfilesState> {
  private var myState: SerialProfilesState = SerialProfilesState()

  override fun getState(): SerialProfilesState {
    return myState
  }

  override fun loadState(state: SerialProfilesState) {
    myState = state
  }

  fun setDefaultProfile(defaultProfile: SerialPortProfile) {
    myState.defaultProfile = defaultProfile
  }

  fun setProfiles(profiles: MutableMap<String, SerialPortProfile>) {
    myState.profiles = profiles
  }

  fun getProfiles(): Map<String, SerialPortProfile> = myState.profiles

  fun copyDefaultProfile(portName: @NlsSafe String? = null): SerialPortProfile {
    val profile = myState.defaultProfile.copy()
    if(portName!=null) profile.portName = portName
    return profile
  }

  fun defaultBaudRate(): Int = myState.defaultProfile.baudRate

  enum class NewLine(private val displayKey: String, val value: String) {
    CR("uart.newline.cr", "\r"),
    LF("uart.newline.lf", "\n"),
    CRLF("uart.newline.crlf", "\r\n");

    override fun toString() = SerialMonitorBundle.message(displayKey)
  }

  class PresentableName : NameGetter() {
    override fun get(): String = SerialMonitorBundle.message("presentable.name")
  }

}

