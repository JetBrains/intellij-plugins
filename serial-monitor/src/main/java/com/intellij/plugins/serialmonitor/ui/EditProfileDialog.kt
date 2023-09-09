package com.intellij.plugins.serialmonitor.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.openapi.ui.validation.validationErrorFor
import com.intellij.openapi.ui.validation.validationErrorIf
import com.intellij.plugins.serialmonitor.*
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.ui.ComboboxSpeedSearch
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.application
import com.intellij.util.ui.JBFont
import org.jetbrains.annotations.Nls
import java.nio.charset.Charset
import javax.swing.JComponent
import javax.swing.JLabel
import kotlin.reflect.KMutableProperty1

open class EditProfileDialog(
  private val connectPanel: ConnectPanel,
  srcProfile: SerialPortProfile,
  private var suggestedName: String,
  private val originalName: String?
) : DialogWrapper(null, connectPanel, false, IdeModalityType.PROJECT) {

  private val profile = srcProfile.copy()
  private var newProfileLabel: Cell<JLabel>? = null

  init {
    title = "Modify Serial Profile"
    init()
  }

  final override fun init() {super.init()}

  override fun createCenterPanel(): JComponent {
    var portCombobox: ComboBox<String>? = null
    return panel {
      row {
        textField()
          .label("Profile Name:")
          .trimmedTextValidation(
            validationErrorIf("Please enter name", String::isEmpty),
            validationErrorFor<String> {
              if (it == originalName || !application.service<SerialProfileService>().getProfiles().contains(it)) return@validationErrorFor null
              @Nls val msg = "Profile $it already exists"
              msg
            }
          )
          .text(suggestedName).whenTextChangedFromUi(disposable) {
            suggestedName = it.trim()
            checkNewProfile()
          }
          .align(Align.FILL).resizableColumn()
      }.layout(RowLayout.LABEL_ALIGNED)
      row {
        placeholder()
        newProfileLabel = label(" ").applyToComponent {
          font = JBFont.small()
          foreground = JBColor.namedColor("Label.infoForeground")
        }
        checkNewProfile()
      }.layout(RowLayout.LABEL_ALIGNED)
      row {
        val portValidation = DialogValidation.WithParameter<ComboBox<String>> {
          DialogValidation {
            val text = it.editor.item?.toString()
            if (text.isNullOrBlank()) return@DialogValidation ValidationInfoBuilder(it).error("Please enter port name")
            if (!portNames().contains(text)) return@DialogValidation ValidationInfoBuilder(it).warning(
              "Port does not exists")
            return@DialogValidation null
          }
        }
        comboBox(portNames().sorted())
          .label("Port:")
          .validationOnInput(portValidation)
          .applyToComponent {
            isEditable = true
            editor.item = profile.portName
            portCombobox = this
          }
      }.layout(RowLayout.LABEL_ALIGNED)
      serialSettings(disposable, profile) { }
      onApply { //workaround: editable combobox does not send any  events when the text is changed
        profile.portName = portCombobox?.editor?.item?.toString() ?: ""
      }
    }.apply {
      registerValidators(disposable)
      validateAll()
    }


  }

  private fun portNames() = service<SerialPortService>().getPortsNames()

  private fun checkNewProfile() {
    if (suggestedName == originalName || application.service<SerialProfileService>().getProfiles().contains(suggestedName)) {
      newProfileLabel?.component?.text = " "
    }
    else {
      newProfileLabel?.component?.text = "New profile"
    }
  }

  fun ask(): Pair<@Nls String?, SerialPortProfile?> {
    if (showAndGet())
      return suggestedName to profile
    else return null to null
  }

  companion object {
    private val charsets: Collection<String> = Charset.availableCharsets().filter { it.value.canEncode() }.keys

    fun Panel.serialSettings(disposable: Disposable, profile: SerialPortProfile, save: () -> Unit) {
      fun <T> Cell<ComboBox<T>>.speedSearch(): Cell<ComboBox<T>> {
        ComboboxSpeedSearch.installOn(component)
        return this
      }

      fun Cell<JBCheckBox>.changesBind(prop: KMutableProperty1<SerialPortProfile, Boolean>): Cell<JBCheckBox> {
        this.component.isSelected = prop.get(profile)
        this.whenStateChangedFromUi (disposable) {
          val value = component.isSelected
          if (value != prop.get(profile)) {
            prop.set(profile, value)
            save()
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
            save()
          }
        }
        return this
      }
      row {
        comboBox(StandardBauds)
          .changesBind(SerialPortProfile::baudRate)
          .speedSearch()
          .label(SerialMonitorBundle.message("label.baud"))
          .focused()
        comboBox(SerialBits)
          .changesBind(SerialPortProfile::bits)
          .label(SerialMonitorBundle.message("label.bits"))
        comboBox(listOf(*Parity.values()))
          .changesBind(SerialPortProfile::parity)
          .label(SerialMonitorBundle.message("label.parity"))
        comboBox(listOf(*StopBits.values()))
          .changesBind(SerialPortProfile::stopBits)
          .label(SerialMonitorBundle.message("label.stop.bits"))
      }.layout(RowLayout.LABEL_ALIGNED)
      row {
        comboBox(listOf(*SerialProfileService.NewLine.values()))
          .changesBind(SerialPortProfile::newLine)
          .label(SerialMonitorBundle.message("label.new.line"))
        comboBox(charsets)
          .speedSearch()
          .changesBind(SerialPortProfile::encoding)
          .label(SerialMonitorBundle.message("label.encoding"))
        checkBox("")
          .label("Local Echo")
          .changesBind(SerialPortProfile::localEcho)
      }.layout(RowLayout.LABEL_ALIGNED)
    }

  }
}
