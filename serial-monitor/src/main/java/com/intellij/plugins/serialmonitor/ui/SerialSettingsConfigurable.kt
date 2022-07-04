package com.intellij.plugins.serialmonitor.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.MasterDetailsComponent.MyNode
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.StringUtil
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.SerialProfileService
import com.intellij.plugins.serialmonitor.SerialProfileService.*
import com.intellij.plugins.serialmonitor.service.JsscSerialService
import com.intellij.plugins.serialmonitor.service.SerialSettingsChangeListener
import com.intellij.ui.ComboboxSpeedSearch
import com.intellij.ui.UIBundle
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.UIUtil
import java.nio.charset.Charset
import java.util.function.BiPredicate
import javax.swing.*

@NlsSafe
private const val WINDOWS_DEF_SERIAL_PORT = "COM1"

@NlsSafe
private const val NIX_DEF_SERIAL_PORT = "/dev/ttyS0"

private val charsets: List<String> = //todo move most popular charsets to the top
  Charset.availableCharsets().filter { e ->
    val charset = e.value
    charset.canEncode() && charset.newEncoder().maxBytesPerChar() == 1.0f && charset.newDecoder().maxCharsPerByte() == 1.0f
  }.map(Map.Entry<String, Any>::key)

private val StandardBauds: List<Int> = listOf(300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400)

private val SerialBits: List<Int> = listOf(8, 7, 6, 5)

internal data class NamedSerialPortProfile(@NlsSafe var name: String, val serialPortProfile: SerialPortProfile)

@Suppress("UNCHECKED_CAST")
internal class Helper(val defaultSerialPortProfile: SerialPortProfile) : Namer<NamedSerialPortProfile>, Cloner<NamedSerialPortProfile>, Factory<NamedSerialPortProfile>,
                                                                         BiPredicate<NamedSerialPortProfile, NamedSerialPortProfile>, Comparator<MyNode> {

  override fun getName(item: NamedSerialPortProfile): String =
    if (isDefault(item)) SerialMonitorBundle.message("default.profile.name") else item.name

  override fun canRename(item: NamedSerialPortProfile): Boolean = !isDefault(item)

  override fun setName(t: NamedSerialPortProfile, name: String) {
    t.name = name
  }

  override fun create(): NamedSerialPortProfile {
    return NamedSerialPortProfile(name = "", serialPortProfile = defaultSerialPortProfile.copy())
  }

  fun create(name: String, portName: String): NamedSerialPortProfile {
    return NamedSerialPortProfile(name = name, serialPortProfile = defaultSerialPortProfile.copy(portName = portName))
  }

  override fun cloneOf(t: NamedSerialPortProfile): NamedSerialPortProfile {
    return if (isDefault(t)) t else copyOf(t)
  }

  override fun copyOf(t: NamedSerialPortProfile): NamedSerialPortProfile = t.copy(serialPortProfile = t.serialPortProfile.copy())

  override fun test(t: NamedSerialPortProfile, u: NamedSerialPortProfile): Boolean = t == u

  @Suppress("UNCHECKED_CAST")
  override fun compare(node1: MyNode, node2: MyNode): Int {
    val default1 = isDefault((node2.userObject as NamedConfigurable<NamedSerialPortProfile>).editableObject)
    val default2 = isDefault((node1.userObject as NamedConfigurable<NamedSerialPortProfile>).editableObject)
    if (default1) {
      return if (default2) 0 else 1
    }
    if (default2) {
      return -1
    }
    return StringUtil.naturalCompare(node1.displayName, node2.displayName)
  }

  fun allProfiles(): MutableList<NamedSerialPortProfile> {
    val serialProfileService = SerialProfileService.getInstance()
    val profilesList = mutableListOf(NamedSerialPortProfile(name = "", defaultSerialPortProfile))
    profilesList.addAll(serialProfileService.getProfiles().map { NamedSerialPortProfile(it.key, it.value) })
    return profilesList
  }

  fun isDefault(profile: NamedSerialPortProfile?): Boolean = defaultSerialPortProfile === profile?.serialPortProfile
}

internal class SerialSettingsConfigurable(private val helper: Helper) :
  SearchableConfigurable,
  NamedItemsListEditor<NamedSerialPortProfile>(helper, helper, helper, helper, helper.allProfiles(), false) {

  init {
    setShowIcons(true)
    reset()
    initTree()
  }

  override fun getNodeComparator() = helper

  override fun getId(): String = "serialmonitor.settings"
  override fun getHelpTopic(): String = id
  override fun getDisplayName(): String = SerialMonitorBundle.message("configurable.name.serial.settings")
  override fun createConfigurable(profile: NamedSerialPortProfile): UnnamedConfigurable {
    return SerialProfileConfigurable(profile, helper.isDefault(profile))
  }

  override fun apply() {
    super.apply()
    val profiles = mutableMapOf<String, SerialPortProfile>()
    items.forEach {
      if (!helper.isDefault(it)) {
        val name = it.name
        if (name.isBlank()) {
          throw ConfigurationException(UIBundle.message("master.detail.err.empty.name"))
        }
        if (profiles.put(name, it.serialPortProfile) != null) {
          throw ConfigurationException(SerialMonitorBundle.message("dialog.message.duplicated.profile.name", it.name)); }
      }
    }
    SerialProfileService.getInstance().apply {
      setDefaultProfile(helper.defaultSerialPortProfile)
      setProfiles(profiles)
    }
    ApplicationManager.getApplication().messageBus.syncPublisher(SerialSettingsChangeListener.TOPIC).settingsChanged()
  }

  override fun getNewLabelText(): String {
    return SerialMonitorBundle.message("label.new.profile.name")
  }

  override fun createItem(): NamedSerialPortProfile? {
    val portNames = JsscSerialService.getPortNames().toTypedArray()
    var portName = if (portNames.isNotEmpty()) portNames[0]
    else
      systemDefaultPortName()
    portName =
      Messages.showEditableChooseDialog(SerialMonitorBundle.message("dialog.message.port"),
                                        SerialMonitorBundle.message("dialog.title.create.serial.connection.profile"),
                                        AllIcons.Nodes.Plugin,
                                        portNames, portName, null)
    if (portName.isNullOrBlank()) return null
    var profileName = portName

    if (currentItems.find { it.name == profileName } != null) {
      for (index in 1..999) {
        profileName = "$portName ($index)"
        if (currentItems.find { it.name == profileName } == null) break
      }
    }
    return helper.create(name = profileName, portName = portName)
  }

  override fun canDelete(item: NamedSerialPortProfile?): Boolean = !helper.isDefault(item)

  override fun canCopy(item: NamedSerialPortProfile?): Boolean = !helper.isDefault(item)

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
          }.layout(RowLayout.PARENT_GRID)
        }
        row {
          comboBox(StandardBauds).bindItem(portProfile::baudRate.toNullableProperty(0)).label(
            SerialMonitorBundle.message("label.baud")).focused()
          comboBox(SerialBits).bindItem(portProfile::bits.toNullableProperty(0)).label(SerialMonitorBundle.message("label.bits"))
        }.layout(RowLayout.PARENT_GRID)
        row {
          comboBox(listOf(*Parity.values())).bindItem(portProfile::parity) { portProfile.parity = it!! }.label(
            SerialMonitorBundle.message("label.parity"))
          comboBox(listOf(*StopBits.values())).bindItem(portProfile::stopBits) { portProfile.stopBits = it!! }.label(
            SerialMonitorBundle.message("label.stop.bits"))
        }.layout(RowLayout.PARENT_GRID)
        row {
          comboBox(listOf(*NewLine.values())).bindItem(portProfile::newLine) { portProfile.newLine = it!! }.label(
            SerialMonitorBundle.message("label.new.line"))
          comboBox(charsets)
            .applyToComponent { ComboboxSpeedSearch(this) }
            .bindItem(portProfile::encoding) { portProfile.encoding = it!! }
            .label(SerialMonitorBundle.message("label.encoding"))
        }.layout(RowLayout.PARENT_GRID)

      }.apply { border = JBEmptyBorder(UIUtil.PANEL_REGULAR_INSETS) }
    }

    private fun systemDefaultPortName(): @NlsSafe String = if (SystemInfo.isWindows) WINDOWS_DEF_SERIAL_PORT else NIX_DEF_SERIAL_PORT

    private fun allPortNames(systemPortNames: List<String>, savedPortName: String): Collection<String> {
      val portNames = systemPortNames.toSortedSet()
      portNames.add(savedPortName)
      if (portNames.isEmpty()) {
        portNames.add(systemDefaultPortName())
      }
      return portNames
    }

    class SerialProfileConfigurable(namedProfile: NamedSerialPortProfile,
                                    defaultProfile: Boolean) : UnnamedConfigurable, Iconable {
      private val myPanel: DialogPanel = createSettingsPanel(namedProfile.serialPortProfile, defaultProfile, true)


      override fun getIcon(flags: Int): Icon = AllIcons.Nodes.Plugin

      override fun createComponent(): JComponent = myPanel
      override fun apply() = myPanel.apply()
      override fun isModified(): Boolean = myPanel.isModified()
    }

  }
}

class SerialSettingsConfigurableProvider : ConfigurableProvider() {
  override fun createConfigurable(): Configurable {
    val defaultProfile = SerialProfileService.getInstance().copyDefaultProfile()
    val helper = Helper(defaultProfile)
    return SerialSettingsConfigurable(helper)
  }
}