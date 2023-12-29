package com.intellij.plugins.serialmonitor.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.Parity
import com.intellij.plugins.serialmonitor.SerialBits
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.SerialProfileService
import com.intellij.plugins.serialmonitor.StandardBauds
import com.intellij.plugins.serialmonitor.StopBits
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle.*
import com.intellij.ui.ComboboxSpeedSearch
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.whenItemSelectedFromUi
import com.intellij.ui.dsl.builder.whenStateChangedFromUi
import com.intellij.ui.layout.ValidationInfoBuilder
import icons.SerialMonitorIcons
import org.jetbrains.annotations.NonNls
import java.nio.charset.Charset
import kotlin.reflect.KMutableProperty1

private val charsets: Collection<String> = Charset.availableCharsets().filter { it.value.canEncode() }.keys

private val namePattern = Regex("(.+)\\((\\d+)\\)\\s*")

fun ConnectableList.createNewProfile(oldProfileName: String?, newPortName: String? = null) {

  val service = service<SerialProfileService>()
  val profiles = service.getProfiles().toMutableMap()
  val newProfile =
    if (oldProfileName == null) {
      service.copyDefaultProfile(newPortName)
    }
    else {
      profiles.getOrElse(oldProfileName) { service.copyDefaultProfile(newPortName) }
    }
  var i = 0
  var nameBase = oldProfileName ?: newProfile.defaultName()
  var newName: String? = nameBase
  namePattern.matchEntire(nameBase)?.also {
    nameBase = it.groupValues[1].trimEnd()
    i = it.groupValues[2].toInt()
    newName = null
  }
  while (newName == null || profiles.containsKey(newName)) {
    i++
    newName = "$nameBase ($i)"
  }
  val validator = object : InputValidatorEx {
    override fun checkInput(inputString: @NlsSafe String): Boolean = inputString.isNotBlank() && !profiles.containsKey(inputString)
    override fun canClose(inputString: @NlsSafe String): Boolean = checkInput(inputString)
    override fun getErrorText(inputString: @NonNls String): @DetailedDescription String? {
      if(inputString.isBlank()) return message("text.enter.unique.profile.name")
      if (checkInput(inputString)) return null
      return message("text.profile.already.exists")
    }
  }
  val finalName = Messages.showInputDialog(this, message("dialog.message.name"), message("dialog.title.new.profile"),
                                           null, newName, validator)
  if (finalName != null) {
    profiles[finalName] = newProfile
    service.setProfiles(profiles)
    this.rescanProfiles(finalName)
  }
}

fun Panel.serialSettings(disposable: Disposable,
                         profile: SerialPortProfile,
                         readOnly: Boolean = false,
                         save: (SerialPortProfile) -> Unit) {

  fun <T> Cell<ComboBox<T>>.speedSearch(): Cell<ComboBox<T>> = this.applyToComponent { ComboboxSpeedSearch.installOn(this) }

  fun Cell<JBCheckBox>.changesBind(prop: KMutableProperty1<SerialPortProfile, Boolean>): Cell<JBCheckBox> {
    this.component.isSelected = prop.get(profile)
    this.whenStateChangedFromUi(disposable) {
      val value = component.isSelected
      if (value != prop.get(profile)) {
        prop.set(profile, value)
        save(profile)
      }
    }
    return this
  }

  fun <P> Cell<ComboBox<P>>.changesBind(prop: KMutableProperty1<SerialPortProfile, P>): Cell<ComboBox<P>> {
    this.component.selectedItem = prop.get(profile)
    this.whenItemSelectedFromUi(disposable) {
      @Suppress("UNCHECKED_CAST") val value = component.selectedItem as? P
      if (value != null && value != prop.get(profile)) {
        prop.set(profile, value)
        save(profile)
      }
    }
    return this
  }
  row {
    comboBox(StandardBauds)
      .changesBind(SerialPortProfile::baudRate)
      .speedSearch()
      .label(message("label.baud"))
      .enabled(!readOnly)
      .focused()
    comboBox(SerialBits)
      .changesBind(SerialPortProfile::bits)
      .enabled(!readOnly)
      .label(message("label.bits"))
    comboBox(Parity.entries)
      .changesBind(SerialPortProfile::parity)
      .enabled(!readOnly)
      .label(message("label.parity"))
    comboBox(StopBits.entries)
      .changesBind(SerialPortProfile::stopBits)
      .enabled(!readOnly)
      .label(message("label.stop.bits"))
  }.layout(RowLayout.LABEL_ALIGNED)
  row {
    comboBox(SerialProfileService.NewLine.entries)
      .changesBind(SerialPortProfile::newLine)
      .enabled(!readOnly)
      .label(message("label.new.line"))
    comboBox(charsets)
      .speedSearch()
      .changesBind(SerialPortProfile::encoding)
      .enabled(!readOnly)
      .label(message("label.encoding"))
    checkBox("")
      .label(message("label.local.echo"))
      .enabled(!readOnly)
      .changesBind(SerialPortProfile::localEcho)
  }.layout(RowLayout.LABEL_ALIGNED)
}


fun portSettings(connectableList: ConnectableList, portName: String, disposable: Disposable): DialogPanel {
  val portStatus = service<SerialPortService>().portStatus(portName)
  return panel {
    row {
      label(message("label.port.name", portName)).applyToComponent {
        if (portStatus == PortStatus.CONNECTED)
          icon = SerialMonitorIcons.ConnectActive
      }
    }

    serialSettings(profile = service<SerialProfileService>().copyDefaultProfile(portName),
                   readOnly = (portStatus != PortStatus.DISCONNECTED) && (portStatus != PortStatus.READY),
                   disposable = disposable) {
      service<SerialProfileService>().setDefaultProfile(it)
    }
    row {

      if (portStatus == PortStatus.READY) {
        button(message("button.connect")) {
          val profile = service<SerialProfileService>().copyDefaultProfile(portName)
          connectableList.parent.connectProfile(profile)
        }
      }

      if (portStatus == PortStatus.CONNECTED) {
        button(message("button.disconnect")) {
          connectableList.parent.disconnectPort(portName)
        }
      }

      if (portStatus != PortStatus.READY && portStatus != PortStatus.BUSY) {
        button(message("button.open.console")) {
          connectableList.parent.openConsole(portName)
        }
      }
      link(message("link.label.create.profile")) { connectableList.createNewProfile(null, portName) }
    }
  }
}

fun profileSettings(connectableList: ConnectableList, disposable: Disposable): DialogPanel? {
  val (profileName, profile) = connectableList.getSelectedProfile() ?: (null to null)
  if (profile != null && profileName != null) {
    var portCombobox: ComboBox<String>? = null
    val status = service<SerialPortService>().portStatus(profile.portName)
    return panel {
      row {
        label(message("label.profile", profileName)).applyToComponent {
          icon = if (status == PortStatus.CONNECTED)
            SerialMonitorIcons.ConnectActive
          else
            AllIcons.Nodes.EmptyNode
        }
      }
      row {
        val portNames = service<SerialPortService>().getPortsNames()
        val portValidation = DialogValidation.WithParameter<ComboBox<String>> {
          DialogValidation {
            val text = it.editor.item?.toString()
            return@DialogValidation when {
              text.isNullOrBlank() -> ValidationInfoBuilder(it).error(message("dialog.message.port.name"))
              !portNames.contains(text) -> ValidationInfoBuilder(it).warning(message("dialog.message.port.does.not.exists"))
              else -> null
            }
          }
        }

        comboBox(portNames).label(message("label.port"))
          .validationOnInput(portValidation)
          .applyToComponent {
            isEditable = true
            editor.item = profile.portName
            portCombobox = this
          }

      }
      serialSettings(disposable = disposable, profile = profile) {
        val service = service<SerialProfileService>()
        val profiles = service.getProfiles().toMutableMap()
        profiles[profileName] = it
        service.setProfiles(profiles)
      }
      row {
        when (status) {
          PortStatus.DISCONNECTED -> {
            button(message("button.connect")) {
              connectableList.parent.connectProfile(profile, profileName)
            }
            button(message("button.open.console")) {
              connectableList.parent.openConsole(profile.portName)
            }
          }
          PortStatus.CONNECTED -> {
            button(message("button.disconnect")) {
              connectableList.parent.disconnectPort(profile.portName)
            }
            button(message("button.open.console")) {
              connectableList.parent.openConsole(profile.portName)
            }
          }
          PortStatus.BUSY,
          PortStatus.CONNECTING,
          PortStatus.UNAVAILABLE_DISCONNECTED -> {
            button(message("button.open.console")) {
              connectableList.parent.openConsole(profile.portName)
            }
          }
          PortStatus.UNAVAILABLE -> {}

          PortStatus.READY -> button(message("button.connect")) {
            connectableList.parent.connectProfile(profile, profileName)
          }

        }
        link(
          message("link.label.duplicate.profile")) { connectableList.createNewProfile(profileName) }
      }
      onApply { //workaround: editable combobox does not send any  events when the text is changed
        profile.portName = portCombobox?.editor?.item?.toString() ?: ""
      }

    }.apply {
      registerValidators(disposable)
      validateAll()
    }
  }
  return null

}
