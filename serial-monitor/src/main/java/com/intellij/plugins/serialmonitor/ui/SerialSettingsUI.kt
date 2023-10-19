package com.intellij.plugins.serialmonitor.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.openapi.util.Disposer
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
import com.intellij.ui.AncestorListenerAdapter
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
import javax.swing.event.AncestorEvent
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
    override fun checkInput(inputString: @NlsSafe String): Boolean = !profiles.containsKey(inputString)
    override fun canClose(inputString: @NlsSafe String): Boolean = checkInput(inputString)
    override fun getErrorText(inputString: @NonNls String): @DetailedDescription String? = if (checkInput(inputString)) null
    else "Profile already exists"
  }
  val finalName = Messages.showInputDialog(this, "Name:", "New profile", null, newName, validator)
  if (finalName != null) {
    profiles[finalName] = newProfile
    service.setProfiles(profiles)
    this.rescanPorts(finalName)
  }

}

private fun Panel.serialSettings(disposable: Disposable? = null,
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
      .label(SerialMonitorBundle.message("label.baud"))
      .enabled(!readOnly)
      .focused()
    comboBox(SerialBits)
      .changesBind(SerialPortProfile::bits)
      .enabled(!readOnly)
      .label(SerialMonitorBundle.message("label.bits"))
    comboBox(Parity.entries)
      .changesBind(SerialPortProfile::parity)
      .enabled(!readOnly)
      .label(SerialMonitorBundle.message("label.parity"))
    comboBox(StopBits.entries)
      .changesBind(SerialPortProfile::stopBits)
      .enabled(!readOnly)
      .label(SerialMonitorBundle.message("label.stop.bits"))
  }.layout(RowLayout.LABEL_ALIGNED)
  row {
    comboBox(SerialProfileService.NewLine.entries)
      .changesBind(SerialPortProfile::newLine)
      .enabled(!readOnly)
      .label(SerialMonitorBundle.message("label.new.line"))
    comboBox(charsets)
      .speedSearch()
      .changesBind(SerialPortProfile::encoding)
      .enabled(!readOnly)
      .label(SerialMonitorBundle.message("label.encoding"))
    checkBox("")
      .label("Local Echo")
      .enabled(!readOnly)
      .changesBind(SerialPortProfile::localEcho)
  }.layout(RowLayout.LABEL_ALIGNED)
}


fun portSettings(connectableList: ConnectableList, portName: String, portStatus: PortStatus): DialogPanel {
  return panel {
    row {
      label("Port $portName").applyToComponent {
        if (portStatus != PortStatus.DISCONNECTED)
          icon = SerialMonitorIcons.ConnectActive
      }
    }

    serialSettings(profile = service<SerialProfileService>().copyDefaultProfile(portName),
                   readOnly = portStatus != PortStatus.DISCONNECTED) {
      service<SerialProfileService>().setDefaultProfile(it)
    }
    row {
      button("Connect") {
        val profile = service<SerialProfileService>().copyDefaultProfile(portName)
        connectableList.parent.connectProfile(profile)
      }
      link("Create profile...") { connectableList.createNewProfile(null, portName) }
    }
  }

}

fun profileSettings(connectableList: ConnectableList): DialogPanel? {
  val (name, profile) = connectableList.getSelectedProfile() ?: (null to null)
  if (profile != null && name != null) {
    var portCombobox: ComboBox<String>? = null
    val portStatus = service<SerialPortService>().portStatus(profile.portName)
    val disposable = Disposer.newDisposable("Serial Profile Parameters")
    return panel {
      row {
        label("Profile $name").applyToComponent {
          if (portStatus != PortStatus.DISCONNECTED)
            icon = SerialMonitorIcons.ConnectActive
        }
      }
      row {
        val portNames = service<SerialPortService>().getPortsNames()
        val portValidation = DialogValidation.WithParameter<ComboBox<String>> {
          DialogValidation {
            val text = it.editor.item?.toString()
            return@DialogValidation when {
              text.isNullOrBlank() -> ValidationInfoBuilder(it).error("Please enter port name")
              !portNames.contains(text) -> ValidationInfoBuilder(it).warning("Port does not exists")
              else -> null
            }
          }
        }

        comboBox(portNames).label("Port:")
          .validationOnInput(portValidation)
          .applyToComponent {
            isEditable = true
            editor.item = profile.portName
            portCombobox = this
          }

      }
      serialSettings(profile = profile) {
        TODO()
      }
      row {
        button("Connect") { connectableList.parent.connectProfile(profile, name) }
        link("Duplicate profile...") { connectableList.createNewProfile(name) }
      }
      onApply { //workaround: editable combobox does not send any  events when the text is changed
        profile.portName = portCombobox?.editor?.item?.toString() ?: ""
      }

    }.apply {
      registerValidators(disposable)
      addAncestorListener(object : AncestorListenerAdapter() {
        override fun ancestorRemoved(event: AncestorEvent) {
          Disposer.dispose(disposable)
        }
      })
      validateAll()
    }
  }
  return null

}
