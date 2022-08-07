package com.intellij.plugins.serialmonitor.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.NamedConfigurable
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.SystemInfo
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.SerialProfileService
import com.intellij.plugins.serialmonitor.service.JsscSerialService
import com.intellij.ui.ComboboxSpeedSearch
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.UIUtil
import java.nio.charset.Charset
import javax.swing.Icon
import javax.swing.JComponent

@NlsSafe
private const val WINDOWS_DEF_SERIAL_PORT = "COM1"

@NlsSafe
private const val NIX_DEF_SERIAL_PORT = "/dev/ttyS0"

private val charsets: List<String> = //todo move most popular charsets to the top
  Charset.availableCharsets().filter { e ->
    val charset = e.value
    charset.canEncode() && charset.newEncoder().maxBytesPerChar() == 1.0f && charset.newDecoder().maxCharsPerByte() == 1.0f
  }.map(Map.Entry<String, Any>::key)

private val StandardBauds: List<Int> = listOf(300, 600, 1200, 2400, 4800, 9600, 19200, 28800, 38400, 57600, 76800, 115200, 230400,
                                              460800, 576000, 921600)

private val SerialBits: List<Int> = listOf(8, 7, 6, 5)

class SerialProfileConfigurable(var name: @NlsContexts.ConfigurableName String,
                                private var originalProfile: SerialPortProfile,
                                val isDefaultProfile: Boolean,
                                private var isNew: Boolean) :
  NamedConfigurable<SerialPortProfile>(), Iconable {

  var profile = originalProfile.copy()

  private val myPanel: DialogPanel = createSettingsPanel(profile, isDefaultProfile, true)
  override fun getDisplayName(): String = if (isDefaultProfile) SerialMonitorBundle.message("default.profile.name") else name

  override fun setDisplayName(name: String) {
    this.name = name
  }

  override fun getEditableObject(): SerialPortProfile = profile

  @Suppress("DialogTitleCapitalization")
  override fun getBannerSlogan(): String = displayName

  override fun getIcon(flags: Int): Icon = AllIcons.Nodes.Plugin

  override fun createOptionsPanel(): JComponent = myPanel
  override fun apply() {
    myPanel.apply()
    isNew = false
    originalProfile = profile.copy()
  }

  override fun isModified(): Boolean {
    if (isNew) return true
    myPanel.apply()
    return originalProfile != profile
  }

  override fun reset() {
    profile.baudRate = originalProfile.baudRate
    profile.bits = originalProfile.bits
    profile.encoding = originalProfile.encoding
    profile.newLine = originalProfile.newLine
    profile.parity = originalProfile.parity
    profile.portName = originalProfile.portName
    profile.stopBits = originalProfile.stopBits
    myPanel.reset()
  }

  override fun getIcon(expanded: Boolean): Icon = AllIcons.Nodes.Plugin

  companion object {
    fun createSettingsPanel(portProfile: SerialPortProfile,
                            defaultProfile: Boolean,
                            portNameSelectable: Boolean): DialogPanel {
      return panel {
        if (!defaultProfile) {
          row {
            comboBox(allPortNames(JsscSerialService.getPortNames(), portProfile.portName))
              .bindItem(
                { portProfile.portName },
                { portProfile.portName = it ?: "" })
              .label(SerialMonitorBundle.message("label.port"))
              .enabled(portNameSelectable)
              .applyIfEnabled()
              .horizontalAlign(HorizontalAlign.FILL)
          }.layout(RowLayout.PARENT_GRID)
        }
        row {
          comboBox(StandardBauds).applyToComponent { isEditable = true }
            .bindItem(portProfile::baudRate.toNullableProperty(0))
            .label(SerialMonitorBundle.message("label.baud"))
            .focused()
          comboBox(SerialBits).bindItem(portProfile::bits.toNullableProperty(0)).label(SerialMonitorBundle.message("label.bits"))
        }.layout(RowLayout.PARENT_GRID)
        row {
          comboBox(listOf(*SerialProfileService.Parity.values())).bindItem(portProfile::parity) { portProfile.parity = it!! }.label(
            SerialMonitorBundle.message("label.parity"))
          comboBox(listOf(*SerialProfileService.StopBits.values())).bindItem(portProfile::stopBits) { portProfile.stopBits = it!! }.label(
            SerialMonitorBundle.message("label.stop.bits"))
        }.layout(RowLayout.PARENT_GRID)
        row {
          comboBox(listOf(*SerialProfileService.NewLine.values())).bindItem(portProfile::newLine) { portProfile.newLine = it!! }.label(
            SerialMonitorBundle.message("label.new.line"))
          comboBox(charsets)
            .applyToComponent { ComboboxSpeedSearch(this) }
            .bindItem(portProfile::encoding) { portProfile.encoding = it!! }
            .label(SerialMonitorBundle.message("label.encoding"))
        }.layout(RowLayout.PARENT_GRID)

      }.apply { border = JBEmptyBorder(UIUtil.PANEL_REGULAR_INSETS) }
    }

    fun systemDefaultPortName(): @NlsSafe String = if (SystemInfo.isWindows) WINDOWS_DEF_SERIAL_PORT else NIX_DEF_SERIAL_PORT

    private fun allPortNames(systemPortNames: List<String>, savedPortName: String): Collection<String> {
      val portNames = systemPortNames.toSortedSet()
      portNames.add(savedPortName)
      if (portNames.isEmpty()) {
        portNames.add(systemDefaultPortName())
      }
      return portNames
    }
  }
}