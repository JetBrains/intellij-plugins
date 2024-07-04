package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.google.gson.JsonParseException
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.icons.AllIcons
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Ref
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ConcurrencyUtil
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.ui.components.BorderLayoutPanel
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.home.PlatformioProjectSettingsStepBase
import com.jetbrains.cidr.cpp.embedded.platformio.project.BoardsJsonParser.parse
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenInstallGuide
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenSettings
import com.jetbrains.cidr.execution.CidrRunProcessUtil
import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

private val EMPTY_TREE_MODEL = DefaultTreeModel(DeviceTreeNode(null, DeviceTreeNode.TYPE.ROOT, "", SourceTemplate.EMPTY_BOARD_INFO))

private val LOGGER = Logger.getInstance(PlatformioProjectSettingsStep::class.java)

class PlatformioProjectSettingsStep(projectGenerator: DirectoryProjectGenerator<Ref<BoardInfo?>>,
                                    callback: AbstractNewProjectStep.AbstractCallback<Ref<BoardInfo?>>) :
  PlatformioProjectSettingsStepBase(projectGenerator, callback) {

  private val myTree: Tree = Tree(EMPTY_TREE_MODEL)
  private var platformioProcessIndicator: ProgressIndicator = EmptyProgressIndicator()
  private val platformioProcessExecutor = ConcurrencyUtil.newSingleScheduledThreadExecutor("PlatformIO Project Setting Process Executor")

  init {
    myTree.cellRenderer = object : ColoredTreeCellRenderer() {
      override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean,
                                         leaf: Boolean, row: Int, hasFocus: Boolean) {
        val node = value as DeviceTreeNode
        append(node.name)
        icon = node.type.icon
      }
    }
    myTree.isRootVisible = false
    myTree.addTreeSelectionListener { e: TreeSelectionEvent ->
      val selectionPath = e.newLeadSelectionPath
      val boardInfo = selectionPath?.lastPathComponent.asSafely<DeviceTreeNode>()?.boardInfo ?: SourceTemplate.EMPTY_BOARD_INFO
      userSelected(boardInfo)
    }
    myTree.emptyText.setText(ClionEmbeddedPlatformioBundle.message("gathering.info"))
    TreeSpeedSearch.installOn(myTree, true) { obj: TreePath ->
      with(obj.lastPathComponent.asSafely<DeviceTreeNode>()) {
        if (this?.type != DeviceTreeNode.TYPE.FRAMEWORK)
          obj.path.joinToString(" ")
        else {
          this.toString()
        }
      }
    }
  }

  private fun watchPlatformio(presense: Presense) {
    application.invokeLater(
      {
        when (presense) {
          Presense.NO -> {
            actionButton.isEnabled = false
            setErrorText(null)
            myTree.setPaintBusy(false)
            myTree.emptyText.setText(
              ClionEmbeddedPlatformioBundle.message("dialog.message.platformio.utility.not.found"))
            myTree.emptyText
              .appendLine(ClionEmbeddedPlatformioBundle.message("open.settings.link"),
                          SimpleTextAttributes.LINK_ATTRIBUTES,
                          OpenSettings(null))
              .appendLine(AllIcons.General.ContextHelp, ClionEmbeddedPlatformioBundle.message("install.guide"),
                          SimpleTextAttributes.LINK_ATTRIBUTES, OpenInstallGuide)
            myTree.model = EMPTY_TREE_MODEL
          }
          Presense.UNKNOWN -> {
            actionButton.isEnabled = false
            setErrorText(null)
            myTree.setPaintBusy(true)
            myTree.emptyText.clear()
            myTree.model = EMPTY_TREE_MODEL
          }
          Presense.YES -> {
            if (myTree.model === EMPTY_TREE_MODEL) {
              myTree.emptyText.setText(ClionEmbeddedPlatformioBundle.message("gathering.info"))
              myTree.invalidate()

              // Cancel the previous process if it is still running before starting the next one
              platformioProcessIndicator.cancel()
              platformioProcessIndicator = EmptyProgressIndicator()

              platformioProcessExecutor.execute {
                try {
                  val commandLine = PlatfromioCliBuilder(false, null)
                    .withParams("boards", "--json-output")
                    .withRedirectErrorStream(true).build()
                  val output = CidrRunProcessUtil.runProcess(CapturingProcessHandler(commandLine), platformioProcessIndicator, 60000)

                  application.invokeLater ({
                    val newModel: DefaultTreeModel
                    if (output.isExitCodeSet && output.exitCode == 0) {
                      val pioOutText = output.stdout
                      try {
                        newModel = DefaultTreeModel(parse(pioOutText))
                      }
                      catch (e: JsonParseException) {
                        LOGGER.warn("Error parsing platformio output: \n\r $pioOutText", e)
                        throw e
                      }
                    }
                    else {
                      newModel = EMPTY_TREE_MODEL
                    }

                    if (output.isTimeout) {
                      setErrorText(ClionEmbeddedPlatformioBundle.message("utility.timeout"))
                    }
                    else if (output.exitCode != 0) {
                      setErrorText(ClionEmbeddedPlatformioBundle.message("platformio.exit.code", output.exitCode))
                    }
                    else {
                      myTree.model = newModel
                      myTree.clearSelection()
                      checkValid()
                      myTree.setPaintBusy(false)
                    }
                  }, ModalityState.stateForComponent(myTree))
                }
                catch (e: Throwable) {
                  LOG.error(e)
                  application.invokeLater {
                    setErrorText(e.message)
                  }
                }
              }
            }
          }
        }
      }, ModalityState.stateForComponent(myTree))
  }

  override fun createAdvancedSettings(): JPanel? {
    val scrollPane = JBScrollPane(myTree)
    startPlatformioWatcher()
    platformioPresent.afterChange(this::watchPlatformio)
    scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
    val panel = BorderLayoutPanel(0, 0)
      .addToTop(JBLabel(
        ClionEmbeddedPlatformioBundle.message("available.boards.frameworks")).withBorder(BorderFactory.createEmptyBorder(0, 5, 3, 0)))
      .addToCenter(scrollPane)
      .withPreferredHeight(0)
      .withBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0))
    checkValid()
    return panel
  }

  private fun userSelected(boardInfo: BoardInfo) {
    peer.settings.set(boardInfo)
    checkValid()
  }

  override fun checkValid(): Boolean {
    if (!super.checkValid()) return false
    if (platformioPresent.get() != Presense.YES) {
      setErrorText(null)
      return false
    }
    val parameters = peer.settings.get().asSafely<BoardInfo>()?.parameters
    if (parameters.isNullOrEmpty()) {
      setWarningText(ClionEmbeddedPlatformioBundle.message("please.select.target"))
      return false
    }
    else {
      setErrorText(null)
      return true
    }
  }

  override fun dispose() {
    platformioProcessIndicator.cancel()
    super.dispose()
  }
}
