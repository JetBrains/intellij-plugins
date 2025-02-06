package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.google.gson.JsonParseException
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.icons.AllIcons
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.openapi.util.Ref
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.asSafely
import com.intellij.util.cancelOnDispose
import com.intellij.util.ui.EDT
import com.intellij.util.ui.components.BorderLayoutPanel
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.home.PlatformioProjectSettingsStepBase
import com.jetbrains.cidr.cpp.embedded.platformio.project.BoardsJsonParser.parse
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenInstallGuide
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenSettings
import com.jetbrains.cidr.execution.CidrRunProcessUtil
import kotlinx.coroutines.*
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

  @Service
  class WatchPlatformioService(private val cs: CoroutineScope) {
    fun watch(presense: Presense, step: PlatformioProjectSettingsStep) {
      // Cancel currently running watches
      cs.coroutineContext.cancelChildren()
      // Run the watch in a new child scope
      val watchContext = (Dispatchers.EDT
                          + ModalityState.any().asContextElement()
                          + CoroutineName("PlatformIO Watch"))
      cs.launch(watchContext){
        step.notifyPlatformioPresense(presense)
      }.cancelOnDispose(step) // Cancel the scope when [step] gets disposed
    }
  }

  private suspend fun notifyPlatformioPresense(presense: Presense) = coroutineScope {
    EDT.assertIsEdt()

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
          loadTree()
        }
      }
    }
  }

  private suspend fun loadTree() = coroutineScope {
    EDT.assertIsEdt()

    myTree.emptyText.setText(ClionEmbeddedPlatformioBundle.message("gathering.info"))
    myTree.invalidate()

    try {
      val commandLine = PlatformioCliBuilder(false, null)
        .withParams("boards", "--json-output")
        .withRedirectErrorStream(true).build()
      val output = withContext(Dispatchers.IO) {
        coroutineToIndicator {
          CidrRunProcessUtil.runWithProgress(CapturingProcessHandler(commandLine), 60_000)
        }
      }

      val newModel: DefaultTreeModel
      if (output.isExitCodeSet && output.exitCode == 0) {
        val pioOutText = output.stdout
        try {
          newModel = withContext(Dispatchers.Default) {
            DefaultTreeModel(parse(pioOutText))
          }
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
    }
    catch(ce: CancellationException) {
      throw ce
    }
    catch(e: Throwable){
      LOG.error(e)
      setErrorText(e.message)
    }
  }

  override fun createPanel(): JPanel? = super.createPanel().also {
    platformioPresent.afterChange { service<WatchPlatformioService>().watch(it, this) }
    startPlatformioWatcher()
  }

  override fun createAdvancedSettings(): JPanel? {
    val scrollPane = JBScrollPane(myTree)
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
