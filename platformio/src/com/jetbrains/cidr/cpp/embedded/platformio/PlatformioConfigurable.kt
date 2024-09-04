package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.validation.validationErrorIf
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.FontUtil
import com.intellij.util.SystemProperties
import com.intellij.util.ui.EDT
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenInstallGuide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JComponent
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isExecutable
import kotlin.io.path.pathString

class PlatformioConfigurable : SearchableConfigurable {

  private var settingsPanel: DialogPanel? = null
  override fun getDisplayName(): @NlsContexts.ConfigurableName String = ClionEmbeddedPlatformioBundle.message(
    "configurable.ClionEmbeddedPlatformioBundle.display.name")

  private var disposable: Disposable? = null
  private fun checkLocation(path: String): Boolean {
    EDT.assertIsEdt()
    if (path.isBlank()) return false
    return adjustPioPath(path).isNullOrEmpty()
  }

  override fun createComponent(): JComponent {
    val newSettingsPanel = panel {
      row {
        val descriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
          .withShowHiddenFiles(true)
          .withTitle(ClionEmbeddedPlatformioBundle.message("dialog.title.home.location"))
        textFieldWithBrowseButton(descriptor)
          .align(Align.FILL)
          .label(ClionEmbeddedPlatformioBundle.message("home.location"), LabelPosition.TOP)
          .bindText(::manualPioLocation.toMutableProperty())
          .trimmedTextValidation(
            validationErrorIf(ClionEmbeddedPlatformioBundle.message("dialog.message.platformio.utility.not.found.inside"), ::checkLocation))
          .applyToComponent {
            EDT.assertIsEdt()
            val location = pioBinLookup()
            val text =
              if (!location.isDirectory())
                ClionEmbeddedPlatformioBundle.message("auto.not.detected.platformio")
              else ClionEmbeddedPlatformioBundle.message("auto.detected.platformio", location.pathString)
            (textField as JBTextField).emptyText.text = text
          }
      }
      row {
        link(ClionEmbeddedPlatformioBundle.message("install.guide"),
             OpenInstallGuide::actionPerformed)
          .applyToComponent {
            font = FontUtil.minusOne(font)
            setContextHelpIcon()
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

  @Service
  class PlatformioConfigurableService(cs: CoroutineScope) {

    private var manualPioLocationValue: AtomicReference<String>

    var manualPioLocation: String
      get() = manualPioLocationValue.get()
      set(path) {
        writeEvents.tryEmit(path)
        manualPioLocationValue.set(path)
      }

    private val writeEvents = MutableSharedFlow<String>(
      replay = 1,
      onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
      cs.launch {
        writeEvents.distinctUntilChanged().collectLatest {
          handlePioPathChange(it)
        }
      }
      manualPioLocationValue = AtomicReference(PropertiesComponent.getInstance().getValue(PIO_LOCATION_KEY, ""))
    }

    private suspend fun handlePioPathChange(path: String) {
      withContext(Dispatchers.IO) {
        PropertiesComponent.getInstance().setValue(PIO_LOCATION_KEY, path.trim())
      }
    }
  }

  companion object {
    private const val ID = "PlatformIO.settings"
    private const val PIO_LOCATION_KEY = "$ID.platformio.location"
    private const val PIO_VENV_FOLDER = "penv"

    private val PIO_EXECUTABLE = "pio" + if (SystemInfo.isWindows) { ".exe" } else { "" }
    private val PLATFORMIO_EXECUTABLE = "platformio" + if (SystemInfo.isWindows) { ".exe" } else { "" }
    private val BIN_FOLDER = if (SystemInfo.isWindows) { "Scripts" } else { "bin" }

    /** Contains what the User has entered as the location of PIO.
     *  Empty if the user did not enter anything.
     */
    private var manualPioLocation: String
      get() = service<PlatformioConfigurableService>().manualPioLocation
      set(path) { service<PlatformioConfigurableService>().manualPioLocation = path }

    fun pioBinFolder(): Path =
      if (manualPioLocation.isNotEmpty()) {
        Path.of(adjustPioPath(manualPioLocation) ?: manualPioLocation)
      }
      else {
        pioBinLookup()
      }

    /** Adjusts the path entered by the user to the bin folder of pio */
    private fun adjustPioPath(pathString: String): String? {
      // Case 1: points to the executable
      val path = Path.of(pathString)
      val fileName = path.fileName?.pathString
      if (path.isExecutable() && (fileName == PIO_EXECUTABLE || fileName == PLATFORMIO_EXECUTABLE)){
        return path.parent?.pathString
      }
      // Else do backup fuzzy search
      return listOf(
        Path.of(pathString, PIO_EXECUTABLE),                              // Case 2: points to bin folder
        Path.of(pathString, BIN_FOLDER, PIO_EXECUTABLE),                  // Case 3: points to venv
        Path.of(pathString, PIO_VENV_FOLDER, BIN_FOLDER, PIO_EXECUTABLE), // Case 4: points to installation folder of platformio
      ).firstOrNull { it.isExecutable() }?.parent?.pathString
    }

    // Searches for pio executable bin folder
    private fun pioBinLookup(): Path {
      val path = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS(PIO_EXECUTABLE)?.parent?.let { Path.of(it) }
      if (path != null) {
        return path
      }
      val defaultPath1 = Path.of(SystemProperties.getUserHome(), ".platformio", PIO_VENV_FOLDER, BIN_FOLDER)
      if (defaultPath1.exists()) return defaultPath1
      val defaultPath2 = Path.of(SystemProperties.getUserHome(), ".pio", PIO_VENV_FOLDER, BIN_FOLDER)
      if (defaultPath2.exists()) return defaultPath2

      return defaultPath1
    }

    fun pioExePath() = pioBinFolder().resolve(PIO_EXECUTABLE).toString()
  }
}
