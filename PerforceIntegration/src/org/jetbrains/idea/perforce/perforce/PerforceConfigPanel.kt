package org.jetbrains.idea.perforce.perforce

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ComponentWithBrowseButton.BrowseFolderActionListener
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.Comparing
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
import org.jetbrains.annotations.Nls
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.*
import org.jetbrains.idea.perforce.perforce.ConfigPanel.*
import org.jetbrains.idea.perforce.perforce.connections.*
import org.jetbrains.idea.perforce.perforce.login.LoginPerformerImpl
import org.jetbrains.idea.perforce.perforce.login.LoginSupport
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager
import java.io.File
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.event.HyperlinkEvent

internal class PerforceConfigPanel(val myProject: Project, val disposable: Disposable) {
  companion object {
    private const val CHARSET_ISO8859_1: @NlsSafe String = "iso8859-1"
    private const val CHARSET_ISO8859_15: @NlsSafe String = "iso8859-15"
    private const val CHARSET_eucjp: @NlsSafe String = "eucjp"
    private const val CHARSET_shiftjis: @NlsSafe String = "shiftjis"
    private const val CHARSET_winansi: @NlsSafe String = "winansi"
    private const val CHARSET_macosroman: @NlsSafe String = "macosroman"
    private const val CHARSET_utf8: @NlsSafe String = "utf8"
  }

  private val myP4EnvHelper = P4EnvHelper.getConfigHelper(myProject)

  init {
    myP4EnvHelper.reset()
  }

  private val myIsEnabled = JBCheckBox(PerforceBundle.message("checkbox.configure.perforce.is.enabled"))
  private val mySwitchToOffline = JBCheckBox(PerforceBundle.message("checkbox.switch.offline"))

  private val myUseP4ConfigRadioButton = JBRadioButton(PerforceBundle.message("checkbox.configure.perforce.use.p4config")).apply {
    addActionListener { updateIgnorePanelHeader() }
  }

  private val myP4ConfigWarningLabel = JBLabel().apply {
    val unsetEnv = myP4EnvHelper.unsetP4EnvironmentVars
    if (unsetEnv.isNotEmpty()) {
      text = PerforceBundle.message("radio.no.p4config.env", unsetEnv)
    }
    RelativeFont.SMALL.install(this)
  }

  private val myUseConnectionParametersButton = JBRadioButton(PerforceBundle.message("connection.params")).apply {
    addActionListener { updateIgnorePanelHeader() }
  }

  private val myPort = JBTextField()
  private val myClient = JBTextField()
  private val myUser = JBTextField()
  private val myCharset = ComboBox<String>()

  private val myIgnorePanelLabel = JBLabel(PerforceBundle.message("border.configure.ignore.settings"))
  private val myUseP4IgnoreRadioButton = JBRadioButton(PerforceBundle.message("checkbox.configure.ignore.use.p4ignore")).apply {
    addActionListener { updateIgnorePanelHeader() }
  }

  private val myP4IgnoreWarningLabel = JBLabel(PerforceBundle.message("radio.no.p4ignore.env")).apply {
    RelativeFont.SMALL.install(this)
  }

  private val myUseIgnoreSettingsRadioButton = JBRadioButton(PerforceBundle.message("ignore.settings")).apply {
    addActionListener { updateIgnorePanelHeader() }
  }

  private val myPathToIgnore = TextFieldWithBrowseButton().apply {
    addBrowseFolderListener(PerforceBundle.message("dialog.title.path.to.p4.ignore"),
                            PerforceBundle.message("dialog.description.path.to.p4.ignore"),
                            myProject,
                            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor())
  }

  private val myShowCmds = JBCheckBox(PerforceBundle.message("checkbox.configure.perforce.log.commands"))
  private val myOutputCmdFileLabel = HyperlinkLabel()

  private val myUseLogin = JBCheckBox(PerforceBundle.message("checkbox.configure.perforce.use.login.authentication"))
  private val myTestConnectionButton = JButton(PerforceBundle.message("button.text.test.connection")).apply {
    addActionListener {
      testConnection()
    }
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

  private val myShowBranchingHistory = JBCheckBox(PerforceBundle.message("checkbox.configure.perforce.show.branching.history"))
  private val myShowIntegratedChangelists = JBCheckBox(PerforceBundle.message("checkbox.configure.perforce.show.integrated.changelists"))

  private val myServerTimeoutField = JBTextField()

  private val myUsePerforceJobs = JBCheckBox(PerforceBundle.message("perforce.use.perforce.jobs"))
  private val myCheckIgnoredUsingP4 = JBCheckBox(PerforceBundle.message("label.configure.perforce.use.p4.for.ignore"))

  val panel = panel {
    if (!myProject.isDefault) {
      row {
        cell(myIsEnabled)
      }
    }
    row {
      cell(mySwitchToOffline)
    }
    if (!myProject.isDefault) {
      configPanel()
      ignorePanel()
        .enabledIf(myUseP4ConfigRadioButton.selected
                     .and(HasIgnoreFileFromEnv(myProject, disposable))
                     .not())
    }
    row {
      cell(myShowCmds)
    }
    indent {
      row(PerforceBundle.message("checkbox.configure.perforce.log.commands.output")) {
        cell(myOutputCmdFileLabel)
      }.layout(RowLayout.INDEPENDENT).enabledIf(myShowCmds.selected)
    }
    row {
      cell(myUseLogin)
      cell(myTestConnectionButton).align(AlignX.RIGHT)
        .visible(!myProject.isDefault)
    }
    parameterRow(PerforceBundle.message("label.configure.perforce.path.to.p4.exe"), myPathToP4)
    parameterRow(PerforceBundle.message("label.configure.perforce.path.to.p4vc.exe"), myPathToP4V)
    row {
      cell(myShowBranchingHistory)
    }
    row {
      cell(myShowIntegratedChangelists)
    }
    row {
      label(PerforceBundle.message("server.timeout")).customize(UnscaledGaps(right = 5))
      cell(myServerTimeoutField).customize(UnscaledGaps(right = 5))
      label(PerforceBundle.message("configure.perforce.timeout.seconds")).customize(UnscaledGaps(right = 5))
    }
    row {
      cell(myUsePerforceJobs)
    }
    row {
      cell(myCheckIgnoredUsingP4)
    }
  }

  private fun Panel.configPanel(): Row {
    return group(PerforceBundle.message("border.configure.perforce.config.settings")) {
      buttonsGroup {
        row {
          cell(myUseP4ConfigRadioButton)
        }
        indent {
          row {
            cell(myP4ConfigWarningLabel).visible(myP4EnvHelper.unsetP4EnvironmentVars.isNotEmpty())
          }
        }
        row {
          cell(myUseConnectionParametersButton)
        }
        indent {
          row {
            panel {
              parameterRow(PerforceBundle.message("label.configure.perforce.port"), myPort)
              parameterRow(PerforceBundle.message("label.configure.perforce.user"), myUser)
            }.resizableColumn()
            panel {
              parameterRow(PerforceBundle.message("label.configure.perforce.client"), myClient)
              parameterRow(PerforceBundle.message("combobox.configure.perforce.charset"), myCharset)
            }.resizableColumn()
          }.enabledIf(myUseConnectionParametersButton.selected)
        }
      }
    }
  }

  private fun Panel.ignorePanel(): Row {
    return group(myIgnorePanelLabel) {
      buttonsGroup {
        row {
          cell(myUseP4IgnoreRadioButton)
        }
        indent {
          row {
            cell(myP4IgnoreWarningLabel).visible(!myP4EnvHelper.hasP4IgnoreSetting())
          }
        }
        row {
          cell(myUseIgnoreSettingsRadioButton)
        }
        indent {
          parameterRow(PerforceBundle.message("ignore.path.to.file"), myPathToIgnore)
            .enabledIf(myUseP4IgnoreRadioButton.selected.not())
        }
      }
    }
  }

  private fun Panel.parameterRow(label: @Nls String, field: JComponent): Row {
    return row(label) {
      cell(field).align(AlignX.FILL)
    }
  }

  init {
    updateIgnorePanelHeader()

    myPathToIgnore.addBrowseFolderListener(PerforceBundle.message("dialog.title.path.to.p4.ignore"),
                                           PerforceBundle.message("dialog.description.path.to.p4.ignore"),
                                           myProject,
                                           FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor())
  }

  fun resetFrom(settings: PerforceSettings) {
    myUseP4ConfigRadioButton.isSelected = settings.useP4CONFIG
    myUseConnectionParametersButton.isSelected = !settings.useP4CONFIG
    myUseP4IgnoreRadioButton.isSelected = settings.useP4IGNORE
    myUseIgnoreSettingsRadioButton.isSelected = !settings.useP4IGNORE
    myPort.text = settings.port
    myClient.text = settings.client
    myUser.text = settings.user
    myShowCmds.isSelected = settings.showCmds
    myPathToP4.text = settings.pathToExec
    myPathToP4V.text = settings.PATH_TO_P4VC
    myPathToIgnore.text = settings.pathToIgnore

    val dumpFile = PerforceRunner.getDumpFile()
    if (dumpFile.exists()) {
      myOutputCmdFileLabel.setHyperlinkText(dumpFile.absolutePath)
    }
    else {
      myOutputCmdFileLabel.setText("'" + dumpFile.absolutePath + "'")
    }
    myOutputCmdFileLabel.addHyperlinkListener { e ->
      if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
        RevealFileAction.openFile(dumpFile)
      }
    }

    myShowBranchingHistory.isSelected = settings.SHOW_BRANCHES_HISTORY
    myUseLogin.isSelected = settings.USE_LOGIN
    myServerTimeoutField.text = (settings.SERVER_TIMEOUT / 1000).toString()
    myUsePerforceJobs.isSelected = settings.USE_PERFORCE_JOBS
    mySwitchToOffline.isSelected = settings.myCanGoOffline
    myShowIntegratedChangelists.isSelected = settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES
    myCheckIgnoredUsingP4.isSelected = !settings.USE_PATTERN_MATCHING_IGNORE

    myCharset.removeAllItems()
    myCharset.addItem(PerforceSettings.getCharsetNone())
    myCharset.addItem(CHARSET_ISO8859_1)
    myCharset.addItem(CHARSET_ISO8859_15)
    myCharset.addItem(CHARSET_eucjp)
    myCharset.addItem(CHARSET_shiftjis)
    myCharset.addItem(CHARSET_winansi)
    myCharset.addItem(CHARSET_macosroman)
    myCharset.addItem(CHARSET_utf8)

    myIsEnabled.isSelected = settings.ENABLED

    myCharset.selectedItem = settings.CHARSET

    updateIgnorePanelHeader()
  }

  fun equalsToSettings(settings: PerforceSettings): Boolean {
    if (connectionPartDiffer(settings)) return false
    if ((settings.SERVER_TIMEOUT / 1000).toString() != myServerTimeoutField.text) return false
    if (settings.showCmds != myShowCmds.isSelected) return false
    if (settings.SHOW_BRANCHES_HISTORY != myShowBranchingHistory.isSelected) return false
    if (settings.pathToExec != myPathToP4.text.trim()) return false
    if (settings.pathToIgnore != myPathToIgnore.text.trim()) return false
    if (settings.useP4IGNORE != myUseP4IgnoreRadioButton.isSelected) return false
    if (settings.PATH_TO_P4VC != myPathToP4V.text.trim()) return false
    if (!Comparing.equal(settings.USE_PERFORCE_JOBS, myUsePerforceJobs.isSelected)) return false
    if (!Comparing.equal(settings.myCanGoOffline, mySwitchToOffline.isSelected)) return false
    if (!Comparing.equal(settings.USE_PATTERN_MATCHING_IGNORE, !myCheckIgnoredUsingP4.isSelected)) return false
    return if (!Comparing.equal<Boolean>(settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES,
                                         myShowIntegratedChangelists.isSelected)) false
    else Comparing.equal<Any>(settings.CHARSET, myCharset.selectedItem)
  }

  private fun connectionPartDiffer(settings: PerforceSettings): Boolean {
    if (settings.useP4CONFIG != myUseP4ConfigRadioButton.isSelected) return true
    if (settings.USE_LOGIN != myUseLogin.isSelected) return true
    if (settings.port != myPort.text.trim()) return true
    if (settings.client != myClient.text.trim()) return true
    if (settings.user != myUser.text.trim()) return true
    return if (settings.ENABLED != myIsEnabled.isSelected) true else false
  }

  fun applyTo(settings: PerforceSettings) {
    applyImpl(settings)
    myP4EnvHelper.reset()
    if (settings.ENABLED != myIsEnabled.isSelected) {
      if (myIsEnabled.isSelected) {
        settings.enable()
      }
      else {
        settings.disable(true)
      }
    }
  }

  private fun applyImpl(settings: PerforceSettings) {
    // todo: bind?
    settings.useP4CONFIG = myUseP4ConfigRadioButton.isSelected
    settings.useP4IGNORE = myUseP4IgnoreRadioButton.isSelected
    settings.port = myPort.text
    settings.client = myClient.text
    settings.user = myUser.text
    settings.showCmds = myShowCmds.isSelected
    settings.pathToIgnore = myPathToIgnore.text
    val execChanged = settings.pathToExec != myPathToP4.text
    settings.pathToExec = myPathToP4.text
    if (execChanged) {
      PerforceManager.getInstance(myProject).resetClientVersion()
    }
    settings.PATH_TO_P4VC = myPathToP4V.text
    settings.CHARSET = myCharset.selectedItem as String
    settings.SHOW_BRANCHES_HISTORY = myShowBranchingHistory.isSelected
    settings.USE_LOGIN = myUseLogin.isSelected
    try {
      settings.SERVER_TIMEOUT = myServerTimeoutField.text.toInt() * 1000
    }
    catch (ex: NumberFormatException) {
      // ignore
    }
    settings.USE_PERFORCE_JOBS = myUsePerforceJobs.isSelected
    settings.myCanGoOffline = mySwitchToOffline.isSelected
    settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES = myShowIntegratedChangelists.isSelected
    settings.USE_PATTERN_MATCHING_IGNORE = !myCheckIgnoredUsingP4.isSelected
  }

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

  private class TestLoginManager constructor(private val myProject: Project,
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