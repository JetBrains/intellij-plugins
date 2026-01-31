package org.jetbrains.idea.perforce.perforce

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.util.whenDisposed
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ComponentWithBrowseButton.BrowseFolderActionListener
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vcs.ex.ProjectLevelVcsManagerEx
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.util.coroutines.childScope
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.not
import com.intellij.ui.layout.or
import com.intellij.ui.layout.selected
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.P4ConfigConnectionDiagnoseDialog
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.connections.P4EnvHelper
import org.jetbrains.idea.perforce.perforce.connections.P4ParamsCalculator
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionProblemsNotifier
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionTester
import org.jetbrains.idea.perforce.perforce.connections.SingletonConnection
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager
import java.io.File
import javax.swing.JEditorPane
import javax.swing.JTextField

private const val CHARSET_NONE: @NlsSafe String = "none"
private const val CHARSET_ISO8859_1: @NlsSafe String = "iso8859-1"
private const val CHARSET_ISO8859_15: @NlsSafe String = "iso8859-15"
private const val CHARSET_eucjp: @NlsSafe String = "eucjp"
private const val CHARSET_shiftjis: @NlsSafe String = "shiftjis"
private const val CHARSET_winansi: @NlsSafe String = "winansi"
private const val CHARSET_macosroman: @NlsSafe String = "macosroman"
private const val CHARSET_utf8: @NlsSafe String = "utf8"

private val charsetValues = listOf(CHARSET_NONE, CHARSET_ISO8859_1, CHARSET_ISO8859_15, CHARSET_eucjp,
                                   CHARSET_eucjp, CHARSET_shiftjis, CHARSET_winansi, CHARSET_macosroman, CHARSET_utf8)

internal class PerforceConfigPanel(private val myProject: Project, private val myDisposable: Disposable, private val cs: CoroutineScope) {
  private val myP4EnvHelper = P4EnvHelper.getConfigHelper(myProject)
  private val mySettings = PerforceSettings.getSettings(myProject)

  private lateinit var myUseP4ConfigRadioButton : JBRadioButton
  private lateinit var myUseP4IgnoreRadioButton : JBRadioButton
  private lateinit var myPort : JBTextField
  private lateinit var myClient : JBTextField
  private lateinit var myUser : JBTextField
  private lateinit var myCharset : ComboBox<String>

  private lateinit var myShowCmds : JBCheckBox
  private lateinit var myUseLogin : JBCheckBox

  private lateinit var myServerTimeoutField : JBTextField

  private lateinit var myConfigEnvWarning : JEditorPane
  private lateinit var myIgnoreEnvWarning : JEditorPane

  private val myIgnorePanelLabel = JBLabel(PerforceBundle.message("border.configure.ignore.settings"))

  private val myPathToIgnore = TextFieldWithBrowseButton().apply {
    addBrowseFolderListener(myProject, FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
      .withTitle(PerforceBundle.message("dialog.title.path.to.p4.ignore"))
      .withDescription(PerforceBundle.message("dialog.description.path.to.p4.ignore")))

  }

  private val myPathToP4 = TextFieldWithBrowseButton().apply {
    addBrowseFolderListener(myProject, FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
      .withTitle(PerforceBundle.message("dialog.title.path.to.p4.exe"))
      .withDescription(PerforceBundle.message("dialog.description.path.to.p4.exe")))
  }

  private val myPathToP4V = TextFieldWithBrowseButton().apply {
    val descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
      .withTitle(PerforceBundle.message("dialog.title.path.to.p4.exe"))
      .withDescription(PerforceBundle.message("dialog.description.path.to.p4vc.exe"))
    addActionListener(object : BrowseFolderActionListener<JTextField?>(this, myProject, descriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT) {
      override fun getInitialFile(): VirtualFile? {
        val file = super.getInitialFile()
        return if (file == null && SystemInfo.isMac) {
          LocalFileSystem.getInstance().refreshAndFindFileByPath("/Applications/p4vc")
        }
        else file
      }
    })
  }

  fun createPanel(): DialogPanel = panel {
    myDisposable.whenDisposed {
      cs.cancel()
    }
    if (!myProject.isDefault) {
      row { checkBox(PerforceBundle.message("checkbox.configure.perforce.is.enabled"))
        .bindSelected({ mySettings.ENABLED }, {
        if (it) {
          mySettings.enable()
        }
        else {
          mySettings.disable(true)
        }
      }) }
    }

    row { checkBox(PerforceBundle.message("checkbox.switch.offline")).bindSelected(mySettings::myCanGoOffline) }
    if (!myProject.isDefault) {
      configPanel()
      ignorePanel()
        .enabledIf(HasIgnoreFileFromEnv(myProject, myDisposable).not()
                     .or(myUseP4ConfigRadioButton.selected.not()))
    }

    row {
      myShowCmds = checkBox(PerforceBundle.message("checkbox.configure.perforce.log.commands"))
        .bindSelected(mySettings::showCmds).component
    }
    indent {
      row(PerforceBundle.message("checkbox.configure.perforce.log.commands.output")) {
        val dumpFile = PerforceRunner.getDumpFile()
        if (dumpFile.exists())
          link(dumpFile.absolutePath) {
            RevealFileAction.openFile(PerforceRunner.getDumpFile())
          }.apply {
            component.autoHideOnDisable = false
          }
        else
          label("'${dumpFile.absolutePath}'")
      }.layout(RowLayout.INDEPENDENT).enabledIf(myShowCmds.selected)
    }
    row {
      myUseLogin = checkBox(PerforceBundle.message("checkbox.configure.perforce.use.login.authentication"))
        .bindSelected(mySettings::USE_LOGIN).component
      button(PerforceBundle.message("button.text.test.connection")) { testConnection() }
        .align(AlignX.RIGHT).visible(!myProject.isDefault)
    }
    row(PerforceBundle.message("label.configure.perforce.path.to.p4.exe")) {
      cell(myPathToP4).align(AlignX.FILL)
        .bindText({ mySettings.pathToExec }, {
          val newText = it.trim()
          val execChanged = mySettings.pathToExec != newText
          mySettings.pathToExec = newText
          if (execChanged) {
            PerforceManager.getInstance(myProject).resetClientVersion()
          }
        })
    }
    row(PerforceBundle.message("label.configure.perforce.path.to.p4vc.exe")) {
      cell(myPathToP4V).align(AlignX.FILL)
        .bindText({ mySettings.PATH_TO_P4VC }, { mySettings.PATH_TO_P4VC = it.trim() })
    }
    row { checkBox(PerforceBundle.message("checkbox.configure.perforce.show.branching.history")).bindSelected(mySettings::SHOW_BRANCHES_HISTORY) }
    row { checkBox(PerforceBundle.message("checkbox.configure.perforce.show.integrated.changelists")).bindSelected(mySettings::SHOW_INTEGRATED_IN_COMMITTED_CHANGES) }
    row {
      label(PerforceBundle.message("server.timeout")).gap(RightGap.SMALL)
      myServerTimeoutField = intTextField().bindIntText({ mySettings.SERVER_TIMEOUT / 1000 }, { mySettings.SERVER_TIMEOUT = it * 1000 })
        .gap(RightGap.SMALL).component
      label(PerforceBundle.message("configure.perforce.timeout.seconds"))
    }
    row { checkBox(PerforceBundle.message("perforce.use.perforce.jobs")).bindSelected(mySettings::USE_PERFORCE_JOBS) }
    row {
      checkBox(PerforceBundle.message("label.configure.perforce.use.p4.for.ignore"))
        .bindSelected({ !mySettings.USE_PATTERN_MATCHING_IGNORE }, { mySettings.USE_PATTERN_MATCHING_IGNORE = !it })
    }
    row {
      checkBox(PerforceBundle.message("label.configure.perforce.forcefully.sync.changelists"))
        .bindSelected(mySettings::FORCE_SYNC_CHANGELISTS)
    }

    onReset {
      updateEnv()
    }

    onApply {
      updateEnv(true)
    }
  }

  private fun Panel.configPanel(): Row = group(PerforceBundle.message("border.configure.perforce.config.settings")) {
    buttonsGroup {
      row(PerforceBundle.message("combobox.configure.perforce.charset")) {
        myCharset = comboBox(charsetValues, textListCellRenderer {
          if (it == CHARSET_NONE) PerforceBundle.message("none.charset.presentation") else it
        }).align(AlignX.FILL)
          .bindItem(mySettings::CHARSET).component
      }
      row {
        myUseP4ConfigRadioButton = radioButton(PerforceBundle.message("checkbox.configure.perforce.use.p4config"), true)
          .configWarningComment().onChanged { updateIgnorePanelHeader() }.component
      }
      row {
        radioButton(PerforceBundle.message("connection.params"), false).onChanged { updateIgnorePanelHeader() }
      }
      indent {
        row(PerforceBundle.message("label.configure.perforce.port")) {
          myPort = textField().align(AlignX.FILL)
            .bindText({ mySettings.port }, { mySettings.port = it.trim() }).component
        }
        row(PerforceBundle.message("label.configure.perforce.user")) {
          myUser = textField().align(AlignX.FILL)
            .bindText(mySettings::user).component
        }
        row(PerforceBundle.message("label.configure.perforce.client")) {
          myClient = textField().align(AlignX.FILL)
            .bindText(mySettings::client).component
        }
      }.enabledIf(myUseP4ConfigRadioButton.selected.not())
    }.bind(mySettings::useP4CONFIG)
  }

  private fun Panel.ignorePanel(): Row = group(myIgnorePanelLabel) {
    buttonsGroup {
      row {
        myUseP4IgnoreRadioButton = radioButton(PerforceBundle.message("checkbox.configure.ignore.use.p4ignore"), true)
          .ignoreWarningComment().onChanged { updateIgnorePanelHeader() }.component
      }
      row {
        radioButton(PerforceBundle.message("ignore.settings"), false).onChanged { updateIgnorePanelHeader() }
      }
      indent {
        row(PerforceBundle.message("ignore.path.to.file")) {
          cell(myPathToIgnore).align(AlignX.FILL)
            .bindText({ mySettings.pathToIgnore }, { mySettings.pathToIgnore = it.trim() })
        }.enabledIf(myUseP4IgnoreRadioButton.selected.not())
      }
    }.bind(mySettings::useP4IGNORE)
  }

  private fun Cell<JBRadioButton>.configWarningComment(): Cell<JBRadioButton> {
    myConfigEnvWarning = comment(PerforceBundle.message("radio.no.p4config.env", "")).comment!!
    return this
  }

  private fun Cell<JBRadioButton>.ignoreWarningComment(): Cell<JBRadioButton> {
    myIgnoreEnvWarning = comment(PerforceBundle.message("radio.no.p4ignore.env")).comment!!
    return this
  }

  private fun testConnection() {
    val isEmpty = PerforceLoginManager.getInstance(myProject).notifier.isEmpty

    val settings = createTestConnectionSettings()
    val tester = PerforceConnectionTester(myProject, settings)

    val isSuccess: Boolean

    if (settings.useP4CONFIG) {
      val result = tester.testConnection()
      val refresher = result.refresher
      isSuccess = !result.isCancelled
      if (result.isCancelled) {
        showCancelledConnectionDialog()
      }
      else if (refresher != null) {
        P4ConfigConnectionDiagnoseDialog(myProject, refresher).show()
      }
    }
    else {
      val result = tester.testConnection(SingletonConnection(myProject, settings))
      isSuccess = !result.isCancelled
      if (result.isCancelled) {
        showCancelledConnectionDialog()
      }
      else if (result.info != null) {
        PerforceConnectionProblemsNotifier.showSingleConnectionState(myProject, result.info)
      }
    }

    val wasConnectionProblems = isSuccess && PerforceConnectionProblemsNotifier.getInstance(myProject).hasConnectionProblems()

    if (!isEmpty || wasConnectionProblems) {
      PerforceConnectionManager.getInstance(myProject).updateConnections()
    }

    updateLabels()
  }

  private fun updateEnv(mappedRootsUpdate: Boolean = false) {
    cs.launch(ModalityState.current().asContextElement()) {
      myP4EnvHelper.reset()
      withContext(Dispatchers.EDT) {
        if (mappedRootsUpdate)
          ProjectLevelVcsManagerEx.getInstanceEx(myProject).scheduleMappedRootsUpdate()
        updateLabels()
      }
    }
  }

  private fun showCancelledConnectionDialog() {
    Messages.showMessageDialog(myProject, PerforceBundle.message("connection.cancelled"),
                               PerforceBundle.message("connection.state.title"), Messages.getErrorIcon())
  }

  private fun createTestConnectionSettings(): PerforceSettings {
    val settings = PerforceSettings(myProject)
    settings.setCanGoOffline(false)
    settings.useP4CONFIG = myUseP4ConfigRadioButton.isSelected
    settings.useP4IGNORE = myUseP4IgnoreRadioButton.isSelected
    settings.port = myPort.text
    settings.client = myClient.text
    settings.user = myUser.text
    settings.CHARSET = myCharset.selectedItem as String
    settings.showCmds = myShowCmds.isSelected
    settings.pathToIgnore = myPathToIgnore.text
    val execChanged = settings.pathToExec != myPathToP4.text
    settings.pathToExec = myPathToP4.text
    if (execChanged) {
      PerforceManager.getInstance(myProject).resetClientVersion()
    }
    settings.PATH_TO_P4VC = myPathToP4V.text
    settings.USE_LOGIN = myUseLogin.isSelected

    val seconds = myServerTimeoutField.text.toIntOrNull() ?: return settings
    settings.SERVER_TIMEOUT = seconds * 1000
    return settings
  }

  private fun updateLabels() {
    if (myProject.isDefault)
      return

    updateIgnorePanelHeader()

    val unsetEnv = myP4EnvHelper.unsetP4EnvironmentVars
    if (unsetEnv.isNotEmpty()) {
      myConfigEnvWarning.text = PerforceBundle.message("radio.no.p4config.env", unsetEnv)
    }
    myConfigEnvWarning.isVisible = unsetEnv.isNotEmpty()
    myIgnoreEnvWarning.isVisible = !myP4EnvHelper.hasP4IgnoreSetting()
  }

  private fun updateIgnorePanelHeader() {
    val enablePanel = !(myUseP4ConfigRadioButton.isSelected && hasIgnoreFileFromEnv())
    myIgnorePanelLabel.text = if (enablePanel) {
      PerforceBundle.message("border.configure.ignore.settings")
    }
    else {
      PerforceBundle.message("border.configure.ignore.settings.disabled")
    }
  }

  private fun hasIgnoreFileFromEnv(): Boolean {
    val configFileName = myP4EnvHelper.p4Config
    if (!myProject.isDefault && myP4EnvHelper.hasP4ConfigSetting()) {
      myP4EnvHelper.hasP4IgnoreSetting()
      val basePath = myProject.basePath!!
      val params = P4ParamsCalculator.getParametersFromConfig(File(basePath), configFileName!!)
      return params.ignoreFileName != null
    }

    return false
  }

  private inner class HasIgnoreFileFromEnv(val myProject: Project, val disposable: Disposable) : ComponentPredicate() {
    override fun addListener(listener: (Boolean) -> Unit) {
      myProject.messageBus.connect(disposable).subscribe(P4EnvHelper.P4_ENV_CHANGED, P4EnvHelper.P4EnvListener {
        listener(invoke())
      })
    }

    override fun invoke(): Boolean = hasIgnoreFileFromEnv()
  }

}

internal class PerforceConfigurable(val myProject: Project, private val cs: CoroutineScope) :
  BoundConfigurable(PerforceVcs.NAME, "project.propVCSSupport.VCSs.Perforce") {

  override fun createPanel(): DialogPanel = PerforceConfigPanel(myProject, disposable!!, cs.childScope("PerforceConfigPanel")).createPanel()
}
