package com.intellij.plugins.serialmonitor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.components.State.NameGetter
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
  fun copyDefaultProfile(): SerialPortProfile = myState.defaultProfile.copy()

  enum class Parity(private val displayKey: String) {
    ODD("uart.parity.odd"), EVEN("uart.parity.even"), NONE("uart.parity.none");

    override fun toString() = SerialMonitorBundle.message(displayKey)
  }

  enum class StopBits(private val displayKey: String) {
    BITS_1("uart.stopbits.1"), BITS_2("uart.stopbits.2"), BITS_1_5("uart.stopbits.1.5");

    override fun toString() = SerialMonitorBundle.message(displayKey)
  }

  enum class NewLine(private val displayKey: String, val value: String) {
    CR("uart.newline.cr", "\r"),
    LF("uart.newline.lf", "\n"),
    CRLF("uart.newline.crlf", "\r\n");

    override fun toString() = SerialMonitorBundle.message(displayKey)
  }

  class PresentableName : NameGetter() {
    override fun get(): String = SerialMonitorBundle.message("presentable.name")
  }

  companion object {
    @JvmStatic
    fun getInstance(): SerialProfileService = ApplicationManager.getApplication().getService(SerialProfileService::class.java)
  }

}

