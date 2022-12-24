package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.validation.validationErrorIf
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.toNioPath
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import com.intellij.util.SystemProperties
import java.nio.file.Path
import javax.swing.JComponent
import kotlin.io.path.exists

class PlatformioConfigurable : SearchableConfigurable {

  private var settingsPanel: DialogPanel? = null
  override fun getDisplayName(): @NlsContexts.ConfigurableName String = ClionEmbeddedPlatformioBundle.message(
    "configurable.ClionEmbeddedPlatformioBundle.display.name")

  private var disposable: Disposable? = null
  private fun checkHome(path: String): Boolean {
    if (path.isBlank()) return false
    val utilName = if (SystemInfo.isWindows) "pio.exe" else "pio"
    return !Path.of(path, "penv", "Scripts", utilName).toFile().canExecute()
  }

  override fun createComponent(): JComponent {
    var enabledCheckBox: Cell<JBCheckBox>? = null
    val newSettingsPanel = panel {
      row {
        label(ClionEmbeddedPlatformioBundle.message("home.location"))
      }
      indent {
        row {
          enabledCheckBox = checkBox(ClionEmbeddedPlatformioBundle.message("custom.platformio.location.enabled")).align(Align.FILL)
            .bindSelected(::pioLocationEnabled.toMutableProperty())
        }
        row {
          textFieldWithBrowseButton(
            project = null,
            browseDialogTitle = ClionEmbeddedPlatformioBundle.message("dialog.title.home.location"),
            fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withShowHiddenFiles(true))
            .align(Align.FILL)
            .enabledIf(enabledCheckBox!!.component.selected)
            .bindText(::pioLocation.toMutableProperty())
            .trimmedTextValidation(
              validationErrorIf(ClionEmbeddedPlatformioBundle.message("dialog.message.platformio.utility.not.found.inside"), ::checkHome))
        }
      }
    }.apply {
      this@PlatformioConfigurable.disposable = Disposer.newDisposable().also {
        registerValidators(it)
      }
      validateAll()
    }
    settingsPanel = newSettingsPanel
    return newSettingsPanel
  }

  override fun disposeUIResources() {
    super.disposeUIResources()
    disposable?.also { Disposer.dispose(it) }
    disposable = null
  }

  override fun isModified(): Boolean = settingsPanel?.isModified() ?: false

  override fun apply() {
    settingsPanel?.apply()
  }

  override fun reset() {
    settingsPanel?.reset()
  }

  override fun getId(): String = ID

  override fun getHelpTopic(): String = "settings.plugin.platformio"


  companion object {
    private const val ID = "PlatformIO.settings"
    private const val PIO_LOCATION_KEY = "$ID.platformio.location"
    private const val PIO_LOCATION_ENABLED_KEY = "$PIO_LOCATION_KEY.enabled"
    private var pioLocationEnabled: Boolean
      get() = PropertiesComponent.getInstance().getBoolean(PIO_LOCATION_ENABLED_KEY, false)
      set(value) = PropertiesComponent.getInstance().setValue(PIO_LOCATION_ENABLED_KEY, value)
    private var pioLocation: String
      get() = PropertiesComponent.getInstance().getValue(PIO_LOCATION_KEY, "").trim()
      set(value) = PropertiesComponent.getInstance().setValue(PIO_LOCATION_KEY, value.trim())

    fun pioBinFolder(): Path? {
      if (pioLocationEnabled && pioLocation.isNotEmpty()) {
        return Path.of(pioLocation, "penv", "Scripts")
      }
      val path = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("pio")?.parent?.toNioPath()
      if (path != null) {
        return path
      }

      val defaultPath1 = Path.of(SystemProperties.getUserHome(), ".platformio", "penv", "Scripts")
      if (defaultPath1.exists()) return defaultPath1

      val defaultPath2 = Path.of(SystemProperties.getUserHome(), ".pio", "penv", "Scripts")
      return if (defaultPath2.exists()) defaultPath2 else defaultPath1

    }

    fun pioExePath() = pioBinFolder()?.resolve("pio")?.toString() ?: "pio"
  }
}
