package org.jetbrains.idea.perforce.perforce

import com.intellij.application.options.editor.CheckboxDescriptor
import com.intellij.application.options.editor.checkBox
import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ComponentWithBrowseButton.BrowseFolderActionListener
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.RelativeFont
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.layout.*
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.*
import org.jetbrains.idea.perforce.perforce.connections.*
import org.jetbrains.idea.perforce.perforce.login.LoginPerformerImpl
import org.jetbrains.idea.perforce.perforce.login.LoginSupport
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager
import java.io.File
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.event.HyperlinkEvent

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


internal class PerforceConfigurable(val myProject: Project) :
  BoundConfigurable(PerforceVcs.NAME, "project.propVCSSupport.VCSs.Perforce") {

  private val myP4EnvHelper = P4EnvHelper.getConfigHelper(myProject)
  private val settings get() = PerforceSettings.getSettings(myProject)

  private val cdIsEnabled = CheckboxDescriptor(PerforceBundle.message("checkbox.configure.perforce.is.enabled"), { settings.ENABLED }, {
    if (it) {
      settings.enable()
    }
    else {
      settings.disable(true)
    }
  })
  private val cdSwitchToOffline = CheckboxDescriptor(PerforceBundle.message("checkbox.switch.offline"), { settings.myCanGoOffline }, { settings.myCanGoOffline = it })
  private val cdShowCmds = CheckboxDescriptor(PerforceBundle.message("checkbox.configure.perforce.log.commands"), { settings.showCmds }, { settings.showCmds = it })
  private val cdUseLogin = CheckboxDescriptor(PerforceBundle.message("checkbox.configure.perforce.use.login.authentication"), { settings.USE_LOGIN }, { settings.USE_LOGIN = it })
  private val cdShowBranchingHistory = CheckboxDescriptor(PerforceBundle.message("checkbox.configure.perforce.show.branching.history"), { settings.SHOW_BRANCHES_HISTORY }, { settings.SHOW_BRANCHES_HISTORY = it })
  private val cdShowIntegratedChangelists = CheckboxDescriptor(PerforceBundle.message("checkbox.configure.perforce.show.integrated.changelists"), { settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES }, { settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES = it })
  private val cdUsePerforceJobs = CheckboxDescriptor(PerforceBundle.message("perforce.use.perforce.jobs"), { settings.USE_PERFORCE_JOBS }, { settings.USE_PERFORCE_JOBS = it })
  private val cdUseP4ForIgnore = CheckboxDescriptor(PerforceBundle.message("label.configure.perforce.use.p4.for.ignore"), { !settings.USE_PATTERN_MATCHING_IGNORE }, { settings.USE_PATTERN_MATCHING_IGNORE = !it })

  private lateinit var myUseP4ConfigRadioButton : JBRadioButton
  private lateinit var myUseP4IgnoreRadioButton : JBRadioButton
  private lateinit var myPort : JBTextField
  private lateinit var myClient : JBTextField
  private lateinit var myUser : JBTextField
  private lateinit var myCharset : ComboBox<String>

  private lateinit var myShowCmds : JBCheckBox
  private lateinit var myUseLogin : JBCheckBox

  private lateinit var myServerTimeoutField : JBTextField

  private lateinit var myConfigEnvWarning : JLabel
  private lateinit var myIgnoreEnvWarning : JLabel

  private val myIgnorePanelLabel = JBLabel(PerforceBundle.message("border.configure.ignore.settings"))

  private val myPathToIgnore = TextFieldWithBrowseButton().apply {
    addBrowseFolderListener(PerforceBundle.message("dialog.title.path.to.p4.ignore"),
                            PerforceBundle.message("dialog.description.path.to.p4.ignore"),
                            myProject,
                            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor())

  }

  private val myPathToP4 = TextFieldWithBrowseButton().apply {
    addBrowseFolderListener(PerforceBundle.message("dialog.title.path.to.p4.exe"),
                             PerforceBundle.message("dialog.description.path.to.p4.exe"),
                             myProject,
                             FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor())
  }

  private val myPathToP4V = TextFieldWithBrowseButton().apply {
    addActionListener(object : BrowseFolderActionListener<JTextField?>(
      PerforceBundle.message("dialog.title.path.to.p4.exe"),
      PerforceBundle.message("dialog.description.path.to.p4vc.exe"),
      this, myProject,
      FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
      TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT) {
        override fun getInitialFile(): VirtualFile? {
          val file = super.getInitialFile()
          return if (file == null && SystemInfo.isMac) {
            LocalFileSystem.getInstance().refreshAndFindFileByPath("/Applications/p4vc")
          }
          else file
        }
    })
  }


  override fun createPanel(): DialogPanel = panel {
    if (!myProject.isDefault) {
      row { checkBox(cdIsEnabled) }
    }

    row { checkBox(cdSwitchToOffline) }
    if (!myProject.isDefault) {
      configPanel()
      ignorePanel()
        .enabledIf(HasIgnoreFileFromEnv(myProject, disposable!!).not()
                     .or(myUseP4ConfigRadioButton.selected.not()))
    }

    row { myShowCmds = checkBox(cdShowCmds).component }
    indent {
      row(PerforceBundle.message("checkbox.configure.perforce.log.commands.output")) {
        outputHyperLink()
      }.layout(RowLayout.INDEPENDENT).enabledIf(myShowCmds.selected)
    }
    row {
      myUseLogin = checkBox(cdUseLogin).component
      button(PerforceBundle.message("button.text.test.connection")) { testConnection() }
        .align(AlignX.RIGHT).visible(!myProject.isDefault)
    }
    row(PerforceBundle.message("label.configure.perforce.path.to.p4.exe")) {
      cell(myPathToP4).align(AlignX.FILL)
        .bindText({ settings.pathToExec }, {
          val newText = it.trim()
          val execChanged = settings.pathToExec != newText
          settings.pathToExec = newText
          if (execChanged) {
            PerforceManager.getInstance(myProject).resetClientVersion()
          }
        })
    }
    row(PerforceBundle.message("label.configure.perforce.path.to.p4vc.exe")) {
      cell(myPathToP4V).align(AlignX.FILL)
        .bindText({ settings.PATH_TO_P4VC }, { settings.PATH_TO_P4VC = it.trim() })
    }
    row { checkBox(cdShowBranchingHistory) }
    row { checkBox(cdShowIntegratedChangelists) }
    row {
      label(PerforceBundle.message("server.timeout")).customize(UnscaledGaps(right = 5))
      myServerTimeoutField = intTextField().bindIntText({ settings.SERVER_TIMEOUT / 1000 }, { settings.SERVER_TIMEOUT = it * 1000 })
        .customize(UnscaledGaps(right = 5)).component
      label(PerforceBundle.message("configure.perforce.timeout.seconds"))
    }
    row { checkBox(cdUsePerforceJobs).component }
    row { checkBox(cdUseP4ForIgnore) }

    onReset {
      myP4EnvHelper.reset()
      updateLabels()
    }

    onApply {
      myP4EnvHelper.reset()
      updateLabels()
    }
  }

  private fun Panel.configPanel(): Row = group(PerforceBundle.message("border.configure.perforce.config.settings")) {
    buttonsGroup {
      row {
        myUseP4ConfigRadioButton = radioButton(PerforceBundle.message("checkbox.configure.perforce.use.p4config"), true)
          .component.apply { addActionListener { updateIgnorePanelHeader() } }
      }
      indent {
        row {
          myConfigEnvWarning = label("")
            .component.apply { RelativeFont.SMALL.install(this) }
        }
      }
      row {
        radioButton(PerforceBundle.message("connection.params"), false)
          .component.apply { addActionListener { updateIgnorePanelHeader() } }
      }
      indent {
        row {
          panel {
            row(PerforceBundle.message("label.configure.perforce.port")) {
              myPort = textField().align(AlignX.FILL)
                .bindText({ settings.port }, { settings.port = it.trim() }).component
            }
            row(PerforceBundle.message("label.configure.perforce.user")) {
              myUser = textField().align(AlignX.FILL)
                .bindText({ settings.user }, { settings.user = it }).component
            }
          }.resizableColumn()
          panel {
            row(PerforceBundle.message("label.configure.perforce.client")) {
              myClient = textField().align(AlignX.FILL)
                .bindText({ settings.client }, { settings.client = it }).component
            }
            row(PerforceBundle.message("combobox.configure.perforce.charset")) {
              myCharset = comboBox(charsetValues, listCellRenderer<String> { it ->
                text = if (it == CHARSET_NONE) PerforceBundle.message("none.charset.presentation") else it
              }).align(AlignX.FILL)
                .bindItem({ settings.CHARSET }, { settings.CHARSET = it }).component
            }
          }.resizableColumn()
        }.enabledIf(myUseP4ConfigRadioButton.selected.not())
      }
    }.bind({ settings.useP4CONFIG }, { settings.useP4CONFIG = it })
  }

  private fun Panel.ignorePanel(): Row = group(myIgnorePanelLabel) {
    buttonsGroup {
      row {
        myUseP4IgnoreRadioButton = radioButton(PerforceBundle.message("checkbox.configure.ignore.use.p4ignore"), true)
          .component.apply { addActionListener { updateIgnorePanelHeader() } }
      }
      indent {
        row {
          myIgnoreEnvWarning = label(PerforceBundle.message("radio.no.p4ignore.env"))
            .component.apply { RelativeFont.SMALL.install(this) }
        }
      }
      row {
        radioButton(PerforceBundle.message("ignore.settings"), false)
          .component.apply { addActionListener { updateIgnorePanelHeader() } }
      }
      indent {
        row(PerforceBundle.message("ignore.path.to.file")) {
          cell(myPathToIgnore).align(AlignX.FILL)
            .bindText({ settings.pathToIgnore }, { settings.pathToIgnore = it.trim() })
        }.enabledIf(myUseP4IgnoreRadioButton.selected.not())
      }
    }.bind({ settings.useP4IGNORE }, { settings.useP4IGNORE = it })
  }

  private fun Row.outputHyperLink() = cell(HyperlinkLabel().apply {
    val dumpFile = PerforceRunner.getDumpFile()
    if (dumpFile.exists()) {
      setHyperlinkText(dumpFile.absolutePath)
    }
    else {
      setText("'" + dumpFile.absolutePath + "'")
    }
    addHyperlinkListener { e ->
      if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
        RevealFileAction.openFile(dumpFile)
      }
    }
  })

  private fun testConnection() {
    val isEmpty = PerforceLoginManager.getInstance(myProject).notifier.isEmpty

    val settings = PerforceSettings(myProject)
    settings.setCanGoOffline(false)
    applyImpl(settings)

    val connectionManager = TestPerforceConnectionManager(myProject, !settings.useP4CONFIG)
    val testLoginManager = TestLoginManager(myProject, settings, connectionManager)
    val runner = PerforceRunner(connectionManager, settings, testLoginManager)
    if (settings.useP4CONFIG) {
      val connectionTestDataProvider = ConnectionTestDataProvider(myProject, connectionManager, runner)
      ProgressManager.getInstance().runProcessWithProgressSynchronously({ connectionTestDataProvider.refresh() },
                                                                        PerforceBundle.message("connection.test"), false, myProject)
      val dialog = P4ConfigConnectionDiagnoseDialog(myProject, connectionTestDataProvider)
      dialog.show()
    }
    else {
      connectionManager.setSingletonConnection(SingletonConnection(myProject, settings))
      val checker = arrayOfNulls<PerforceClientRootsChecker>(1)
      ProgressManager.getInstance()
        .runProcessWithProgressSynchronously({
                                               val allConnections = connectionManager.getAllConnections()
                                               val cache = ClientRootsCache.getClientRootsCache(myProject)
                                               val info = PerforceInfoAndClient.calculateInfos(allConnections.values, runner, cache)
                                               checker[0] = PerforceClientRootsChecker(info, allConnections)
                                             }, PerforceBundle.message("connection.test"), false, myProject)
      PerforceConnectionProblemsNotifier.showSingleConnectionState(myProject, checker[0])
    }

    // +-, can do better
    if (!isEmpty) {
      PerforceConnectionManager.getInstance(myProject).updateConnections()
    }

    myP4EnvHelper.reset()
    updateLabels()
  }

  private fun applyImpl(settings: PerforceSettings) {
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
    try {
      settings.SERVER_TIMEOUT = myServerTimeoutField.text.toInt() * 1000
    }
    catch (ex: NumberFormatException) {
      // ignore
    }
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

  inner class HasIgnoreFileFromEnv(val myProject: Project, val disposable: Disposable) : ComponentPredicate() {
    override fun addListener(listener: (Boolean) -> Unit) {
      myProject.messageBus.connect(disposable).subscribe(P4EnvHelper.P4_ENV_CHANGED, P4EnvHelper.P4EnvListener {
        listener(invoke())
      })
    }

    override fun invoke(): Boolean = hasIgnoreFileFromEnv()
  }

  private class ConnectionTestDataProvider(private val myProject: Project,
                                           private val myConnectionManager: TestPerforceConnectionManager,
                                           private val myRunner: PerforceRunner) : ConnectionDiagnoseRefresher {
    private var myChecker = PerforceClientRootsChecker()
    private var myInfo = emptyMap<P4Connection, ConnectionInfo>()
    private var myMc: PerforceMultipleConnections? = null

    override fun refresh() {
      val calculator = P4ConnectionCalculator(myProject)
      calculator.execute()
      // !! check connectivity & authorization separately
      myMc = calculator.multipleConnections
      val map = myMc!!.allConnections
      myConnectionManager.multipleConnectionObject = myMc
      myInfo = PerforceInfoAndClient.recalculateInfos(myInfo, map.values, myRunner, ClientRootsCache.getClientRootsCache(myProject)).newInfo
      myChecker = PerforceClientRootsChecker(myInfo, map)
    }

    override fun getMultipleConnections(): PerforceMultipleConnections = myMc!!

    override fun getP4RootsInformation(): P4RootsInformation = myChecker
  }

  private class TestLoginManager(private val myProject: Project,
                                 private val mySettings: PerforceSettings,
                                 private val myConnectionManagerI: PerforceConnectionManagerI) : LoginSupport {
    @Throws(VcsException::class)
    override fun silentLogin(connection: P4Connection): Boolean {
      var password = if (connection is P4ParametersConnection)
        connection.parameters.password
      else
        mySettings.passwd
      val loginPerformer = LoginPerformerImpl(myProject, connection, myConnectionManagerI)
      if (password != null && loginPerformer.login(password).isSuccess) {
        return true
      }

      while (true) {
        password = mySettings.requestForPassword(if (mySettings.useP4CONFIG) connection else null)
        if (password == null) return false
        val login = loginPerformer.login(password)
        if (login.isSuccess) {
          PerforceConnectionManager.getInstance(myProject).updateConnections()
          return true
        }
      }
    }

    override fun notLogged(connection: P4Connection) {}
  }


  private class TestPerforceConnectionManager(private val myProject: Project,
                                              private val mySingleton: Boolean) : PerforceConnectionManagerI {
    private var mySingletonConnection: SingletonConnection? = null
    private var myMc: PerforceMultipleConnections? = null
    fun setSingletonConnection(singletonConnection: SingletonConnection?) {
      mySingletonConnection = singletonConnection
    }

    fun setMultipleConnectionObject(mc: PerforceMultipleConnections?) {
      myMc = mc
    }

    override fun getMultipleConnectionObject(): PerforceMultipleConnections? = myMc

    override fun getAllConnections(): Map<VirtualFile, P4Connection> {
      if (mySingleton) {
        val result: MutableMap<VirtualFile, P4Connection> = LinkedHashMap()
        for (root in ProjectLevelVcsManager.getInstance(myProject).getRootsUnderVcs(PerforceVcs.getInstance(myProject))) {
          result[root] = mySingletonConnection!!
        }
        return result
      }
      return myMc!!.allConnections
    }

    override fun getConnectionForFile(file: File): P4Connection? {
      return if (mySingleton) {
        mySingletonConnection
      }
      else {
        val vf = PerforceConnectionManager.findNearestLiveParentFor(file) ?: return null
        myMc!!.getConnection(vf)
      }
    }

    override fun getConnectionForFile(file: P4File): P4Connection? = if (mySingleton) {
      mySingletonConnection
    }
    else {
      getConnectionForFile(file.localFile)
    }

    override fun getConnectionForFile(file: VirtualFile): P4Connection? = if (mySingleton) {
      mySingletonConnection
    }
    else {
      myMc!!.getConnection(file)
    }

    override fun isSingletonConnectionUsed() = mySingleton

    override fun updateConnections() {}
    override fun isUnderProjectConnections(file: File) = true
    override fun isInitialized() = true
  }
}