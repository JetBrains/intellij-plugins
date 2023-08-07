package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.CapturingProcessRunner
import com.intellij.icons.AllIcons
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Ref
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.asSafely
import com.intellij.util.ui.components.BorderLayoutPanel
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.home.PlatformioProjectSettingsStepBase
import com.jetbrains.cidr.cpp.embedded.platformio.project.BoardsJsonParser.parse
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenInstallGuide
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenSettings
import java.util.concurrent.Callable
import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

private val EMPTY_TREE_MODEL = DefaultTreeModel(DeviceTreeNode(null, DeviceTreeNode.TYPE.ROOT, "", SourceTemplate.EMPTY_BOARD_INFO))

class PlatformioProjectSettingsStep(projectGenerator: DirectoryProjectGenerator<Ref<BoardInfo?>>,
                                    callback: AbstractNewProjectStep.AbstractCallback<Ref<BoardInfo?>>) :
  PlatformioProjectSettingsStepBase(projectGenerator, callback) {

  private val myTree: Tree = Tree(EMPTY_TREE_MODEL)

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
    when (presense) {
      Presense.NO -> {
        actionButton.isEnabled = false
        setErrorText(null)
        myTree.setPaintBusy(false)
        myTree.emptyText.setText(ClionEmbeddedPlatformioBundle.message("dialog.message.platformio.utility.not.found"))
        myTree.emptyText
          .appendLine(ClionEmbeddedPlatformioBundle.message("open.settings.link"), SimpleTextAttributes.LINK_ATTRIBUTES,
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
          SwingUtilities.invokeLater {
            myTree.emptyText.setText(ClionEmbeddedPlatformioBundle.message("gathering.info"))
          }
          val outputFuture =
            ApplicationManager.getApplication().executeOnPooledThread(
              Callable {
                val commandLine = PlatfromioCliBuilder(null)
                  .withParams("boards", "--json-output")
                  .withRedirectErrorStream(true).build()
                CapturingProcessRunner(CapturingProcessHandler(commandLine)).runProcess(60000)
              })
          try {
            val output = outputFuture.get()
            val newModel: DefaultTreeModel
            if (output.isExitCodeSet && output.exitCode == 0) {
              newModel = DefaultTreeModel(parse(output.stdout))
            }
            else {
              newModel = EMPTY_TREE_MODEL
            }
            SwingUtilities.invokeLater {
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
            }
          }
          catch (e: Throwable) {
            LOG.error(e)
            setErrorText(e.message)
          }
        }
      }
    }
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
}
