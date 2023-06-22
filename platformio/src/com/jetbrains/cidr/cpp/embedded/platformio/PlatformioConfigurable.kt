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
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.FontUtil
import com.intellij.util.SystemProperties
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenInstallGuide
import java.io.File
import java.nio.file.Path
import javax.swing.JComponent
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString

class PlatformioConfigurable : SearchableConfigurable {

  private var settingsPanel: DialogPanel? = null
  override fun getDisplayName(): @NlsContexts.ConfigurableName String = ClionEmbeddedPlatformioBundle.message(
    "configurable.ClionEmbeddedPlatformioBundle.display.name")

  private var disposable: Disposable? = null
  private fun checkHome(path: String): Boolean {
    if (path.isBlank()) return false
    val utilName = if (SystemInfo.isWindows) "pio.exe" else "pio"
    return !(Path.of(path, "penv", "Scripts", utilName).toFile().canExecute() ||
             Path.of(path, "penv", "bin", utilName).toFile().canExecute())
  }

  override fun createComponent(): JComponent {
    val newSettingsPanel = panel {
      row {
        textFieldWithBrowseButton(
          project = null,
          browseDialogTitle = ClionEmbeddedPlatformioBundle.message("dialog.title.home.location"),
          fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withShowHiddenFiles(true))
          .align(Align.FILL)
          .label(ClionEmbeddedPlatformioBundle.message("home.location"), LabelPosition.TOP)
          .bindText(::pioLocation.toMutableProperty())
          .trimmedTextValidation(
            validationErrorIf(ClionEmbeddedPlatformioBundle.message("dialog.message.platformio.utility.not.found.inside"), ::checkHome))
          .applyToComponent {
            val location = pioBinFolderPath()
            val text =
              if (!location.second.isDirectory())
                ClionEmbeddedPlatformioBundle.message("auto.not.detected.platformio")
              else if (location.first) ClionEmbeddedPlatformioBundle.message("auto.detected.platformio", location.second)
              else ClionEmbeddedPlatformioBundle.message("auto.detected.platformio", location.second.parent?.parent?.pathString)
            (textField as JBTextField).emptyText.text = text
          }
      }
      row {
        link(ClionEmbeddedPlatformioBundle.message("install.guide"),
             OpenInstallGuide::actionPerformed)
          .applyToComponent {
            font = FontUtil.minusOne(font)
            setExternalLinkIcon()
          }
      }
    }
    this.disposable = Disposer.newDisposable()
    newSettingsPanel.registerValidators(this.disposable!!)
    newSettingsPanel.validateAll()
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
    private var pioLocation: String
      get() {
        val file = File(PropertiesComponent.getInstance().getValue(PIO_LOCATION_KEY, "").trim())
        if (file.canExecute()) {
          if (file.parent.endsWith("penv${File.separator}Scripts", true)) {
            val s = file.parentFile.parentFile.parent
            PropertiesComponent.getInstance().setValue(PIO_LOCATION_KEY, s)
            return s
          }
        }
        return file.path
      }
      set(value) = PropertiesComponent.getInstance().setValue(PIO_LOCATION_KEY, value.trim())

    fun pioBinFolder(): Path {
      if (pioLocation.isNotEmpty()) {
        val path = Path.of(pioLocation, "penv", "Scripts")
        if (path.exists()) return path
        return Path.of(pioLocation, "penv", "bin")
      }
      return pioBinFolderPath().second
    }

    private fun pioBinFolderPath(): Pair<Boolean, Path> {
      val path = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("pio")?.parent?.toNioPath()
      if (path != null) {
        return Pair(true, path)
      }

      var defaultPath1 = Path.of(SystemProperties.getUserHome(), ".platformio", "penv", "Scripts")
      if (defaultPath1.exists()) return Pair(false, defaultPath1)
      defaultPath1 = Path.of(SystemProperties.getUserHome(), ".platformio", "penv", "bin")
      if (defaultPath1.exists()) return Pair(false, defaultPath1)

      var defaultPath2 = Path.of(SystemProperties.getUserHome(), ".pio", "penv", "Scripts")
      if (defaultPath2.exists()) return Pair(false, defaultPath2)
      defaultPath2 = Path.of(SystemProperties.getUserHome(), ".pio", "penv", "bin")
      if (defaultPath2.exists()) return Pair(false, defaultPath2)
      return Pair(false, defaultPath1)
    }

    fun pioExePath() = pioBinFolder().resolve("pio").toString()
  }
}
