package com.intellij.plugins.serialmonitor.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.UiWithModelAccess
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.util.whenTextChangedFromUi
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.*
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle.message
import com.intellij.ui.ComboboxSpeedSearch
import com.intellij.ui.UIBundle
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import java.nio.charset.Charset
import javax.swing.JComponent
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty1

private val charsets: Collection<String> = Charset.availableCharsets().filter { it.value.canEncode() }.keys

private val namePattern = Regex("(.+)\\((\\d+)\\)\\s*")

internal suspend fun ConnectableList.createNewProfile(oldProfileName: String?, newPortName: String? = null) {

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
      if (inputString.isBlank()) return message("text.enter.unique.profile.name")
      if (checkInput(inputString)) return null
      return message("text.profile.already.exists")
    }
  }

  withContext(Dispatchers.UiWithModelAccess) {
    val finalName = Messages.showInputDialog(this@createNewProfile, message("dialog.message.name"), message("dialog.title.new.profile"),
                             null, newName, validator)
    if (finalName != null) {
      profiles[finalName] = newProfile
      service.setProfiles(profiles)
      awaitModelUpdate()
      selectProfile(finalName)
    }
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

  fun Cell<ComboBox<Int>>.intValidationOnInput(): Cell<ComboBox<Int>> = validationOnInput {
    val number = when(val item = it.editor.item) {
      is Int -> item
      is String -> item.toIntOrNull()
      else -> null
    }
    when(number) {
      null -> error(UIBundle.message("please.enter.a.number"))
      in Int.MIN_VALUE..0 -> error(message("please.enter.positive.number"))
      else -> null
    }
  }

  fun <T : JComponent> Cell<T>.stdWidth(): Cell<T> =
    this.widthGroup("portControl")

  row {
    topGap(TopGap.MEDIUM)
    comboBox(StandardBauds)
      .applyToComponent { isEditable = true }
      .editableChangesBind(profile::baudRate.toMutableProperty(), String::toIntOrNull, disposable)
      .speedSearch()
      .label(message("label.baud"))
      .enabled(!readOnly)
      .stdWidth()
      .focused()
      .intValidationOnInput()
    comboBox(SerialBits)
      .changesBind(SerialPortProfile::bits)
      .label(message("label.bits"))
      .enabled(!readOnly)
      .stdWidth()
  }.layout(RowLayout.PARENT_GRID)
  row {
    comboBox(StopBits.entries)
      .changesBind(SerialPortProfile::stopBits)
      .label(message("label.stop.bits"))
      .enabled(!readOnly)
      .stdWidth()
    comboBox(Parity.entries)
      .changesBind(SerialPortProfile::parity)
      .label(message("label.parity"))
      .enabled(!readOnly)
      .stdWidth()
  }.layout(RowLayout.PARENT_GRID)
  row {
    comboBox(SerialProfileService.NewLine.entries)
      .changesBind(SerialPortProfile::newLine)
      .label(message("label.new.line"))
      .enabled(!readOnly)
      .stdWidth()
    comboBox(charsets)
      .speedSearch()
      .changesBind(SerialPortProfile::encoding)
      .label(message("label.encoding"))
      .enabled(!readOnly)
      .stdWidth()
  }.layout(RowLayout.PARENT_GRID)
  row {
    checkBox("")
      .changesBind(SerialPortProfile::localEcho)
      .label(message("label.local.echo"))
      .enabled(!readOnly)

    checkBox("")
      .changesBind(SerialPortProfile::showHardwareControls)
      .label(message("label.show.hardware.flow.control"))
      .enabled(!readOnly)
  }.layout(RowLayout.PARENT_GRID)
}


internal fun portSettings(connectableList: ConnectableList, portName: @NlsSafe String, disposable: Disposable): DialogPanel {
  val portStatus = service<SerialPortService>().portStatus(portName)
  val profileService = service<SerialProfileService>()
  return panel {
    indent {
      row {
        topGap(TopGap.MEDIUM)
        label(portName).label(message("label.port.name"))
      }

      serialSettings(profile = profileService.copyDefaultProfile(portName),
                     readOnly = (portStatus != PortStatus.DISCONNECTED) && (portStatus != PortStatus.READY),
                     disposable = disposable) {
        profileService.setDefaultProfile(it)
      }
      row {
        button(message("button.connect")) {
          val profile = profileService.copyDefaultProfile(portName)
          connectableList.parentPanel.connectProfile(profile)
        }.visible(portStatus.portConnectVisible)
          .applyToComponent { toolTipText = portStatus.connectTooltip }

        button(message("button.disconnect")) {
          connectableList.parentPanel.disconnectPort(portName)
        }.visible(portStatus.disconnectVisible)

        button(message("button.open.console")) {
          connectableList.parentPanel.openConsole(portName)
        }.visible(portStatus.openConsoleVisible)

        link(message("link.label.create.profile")) {
        profileService.cs.launch {
          connectableList.createNewProfile (null, portName)
        }
      }
      }
    }
  }
}

internal fun profileSettings(connectableList: ConnectableList, disposable: Disposable): DialogPanel? {
  val (profileName, profile) = connectableList.getSelectedProfile() ?: return null
  if (profile == null) return null
  val status = service<SerialPortService>().portStatus(profile.portName)
  val profileService = service<SerialProfileService>()
  return panel {
    indent {
      row {
        topGap(TopGap.MEDIUM)
        label(profileName).label(message("label.profile"))
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
          .validation(portValidation)
          .resizableColumn()
          .applyToComponent {
            isEditable = true
            editor.item = profile.portName
          }
          .editableChangesBind(profile::portName.toMutableProperty(), {it}, disposable)

      }.layout(RowLayout.LABEL_ALIGNED)
      serialSettings(disposable = disposable, profile = profile) {
        val profiles = profileService.getProfiles().toMutableMap()
        profiles[profileName] = it
        profileService.setProfiles(profiles)
        connectableList.parentPanel.notifyProfileChanged(profile)
      }
      row {
        button(message("button.connect")) {
          connectableList.parentPanel.connectProfile(profile, profileName)
        }.visible(status.profileConnectVisible).enabled(status.connectEnabled)
        .applyToComponent { toolTipText = status.connectTooltip }

        button(message("button.disconnect")) {
          connectableList.parentPanel.disconnectPort(profile.portName)
        }.visible(status.disconnectVisible)

        button(message("button.open.console")) {
          connectableList.parentPanel.openConsole(profile.portName)
        }.visible(status.openConsoleVisible)

        link(message("link.label.duplicate.profile")) {profileService.cs.launch { connectableList.createNewProfile(profileName) } }
      }
    }
  }.apply {
    registerValidators(disposable)
    validateAll()
  }
}

private fun <T> Cell<ComboBox<T>>.editableChangesBind(prop: MutableProperty<T>, parser: (String)->T?, disposable: Disposable): Cell<ComboBox<T>> {
  component.selectedItem = prop.get()

  val setter: (T?) -> Unit = { value ->
    if (value != null && value != prop.get()) {
      prop.set(value)
    }
  }

  whenItemSelectedFromUi(disposable, setter)
  val textEditorComponent = component.editor.editorComponent as JTextComponent
  textEditorComponent.whenTextChangedFromUi(disposable) {
    setter(parser(it))
  }
  return this
}

private val PortStatus.profileConnectVisible get() = !this.disconnectVisible
private val PortStatus.portConnectVisible get() = this == PortStatus.READY

private val PortStatus.connectEnabled get() = this == PortStatus.READY || this == PortStatus.DISCONNECTED

private val PortStatus.disconnectVisible get() = this == PortStatus.CONNECTED

private val PortStatus.openConsoleVisible get() = this != PortStatus.UNAVAILABLE && this != PortStatus.READY

private val PortStatus.connectTooltip: @Nls String? get() = when (this) {
  PortStatus.UNAVAILABLE, PortStatus.UNAVAILABLE_DISCONNECTED -> message("tooltip.port.unavailable")
  PortStatus.BUSY -> message("tooltip.port.busy")
  PortStatus.CONNECTING -> message("tooltip.port.connecting")
  else -> null
}