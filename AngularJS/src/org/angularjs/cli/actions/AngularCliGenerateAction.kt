package org.angularjs.cli.actions

import com.intellij.execution.configurations.CommandLineTokenizer
import com.intellij.icons.AllIcons
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.EditorTextField
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBList
import com.intellij.ui.speedSearch.ListWithFilter
import com.intellij.util.gist.GistManager
import com.intellij.util.gist.GistManagerImpl
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.JBUI
import icons.JavaScriptLanguageIcons
import org.angularjs.cli.*
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * @author Dennis.Ushakov
 */
class AngularCliGenerateAction : DumbAwareAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return

    val file = e.getData(PlatformDataKeys.VIRTUAL_FILE)
    val editor = e.getData(PlatformDataKeys.FILE_EDITOR)
    val cli = findAngularCliFolder(project, file) ?: return

    val model = DefaultListModel<Blueprint>()
    val list = JBList<Blueprint>(model)
    updateList(list, model, project, cli)
    list.cellRenderer = object: JBList.StripedListCellRenderer() {
      override fun getListCellRendererComponent(list: JList<*>?,
                                                value: Any?,
                                                index: Int,
                                                isSelected: Boolean,
                                                cellHasFocus: Boolean): Component {
        val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        icon = JBUI.scale(EmptyIcon.create(5))
        return component
      }
    }

    val actionGroup = DefaultActionGroup()
    val refresh: AnAction = object : AnAction(JavaScriptLanguageIcons.BuildTools.Refresh) {
      init {
        shortcutSet = CustomShortcutSet(*KeymapManager.getInstance().activeKeymap.getShortcuts("Refresh"))
      }

      override fun actionPerformed(e: AnActionEvent?) {
        (GistManager.getInstance() as GistManagerImpl).invalidateData()
        updateList(list, model, project, cli)
      }
    }
    refresh.registerCustomShortcutSet(refresh.shortcutSet, list)
    actionGroup.addAction(refresh)

    val actionToolbar = ActionManager.getInstance().createActionToolbar("AngularCliGenerate", actionGroup, true)
    actionToolbar.setReservePlaceAutoPopupIcon(false)
    actionToolbar.setMinimumButtonSize(Dimension(22, 22))
    val toolbarComponent = actionToolbar.component
    toolbarComponent.isOpaque = false

    val scroll = ScrollPaneFactory.createScrollPane(list)
    scroll.border = JBUI.Borders.empty()
    val pane = ListWithFilter.wrap(list, scroll, StringUtil.createToStringFunction(Blueprint::class.java))

    val builder = JBPopupFactory.getInstance().createComponentPopupBuilder(pane, list).
        setMayBeParent(true).
        setRequestFocus(true).
        setFocusable(true).
        setFocusOwners(arrayOf<Component>(list)).
        setLocateWithinScreenBounds(true).
        setCancelOnOtherWindowOpen(true).
        setMovable(true).
        setResizable(true).
        setSettingButtons(toolbarComponent).
        setCancelOnWindowDeactivation(false).
        setCancelOnClickOutside(true).
        setDimensionServiceKey(project, "org.angular.cli.generate", true).
        setMinSize(Dimension(JBUI.scale(200), JBUI.scale(200))).
        setCancelButton(IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered))
    val popup = builder.createPopup()
    list.addKeyListener(object: KeyAdapter() {
      override fun keyPressed(e: KeyEvent?) {
        if (e?.keyCode == KeyEvent.VK_ENTER) {
          e?.consume()
          askOptions(project, popup, list.selectedValue as Blueprint, cli, workingDir(editor, file))
        }
      }
    })
    object: DoubleClickListener() {
      override fun onDoubleClick(event: MouseEvent?): Boolean {
        if (list.selectedValue == null) return true
        askOptions(project, popup, list.selectedValue as Blueprint, cli, workingDir(editor, file))
        return true
      }
    }.installOn(list)
    popup.showCenteredInCurrentWindow(project)
  }

  private fun workingDir(editor: FileEditor?, file: VirtualFile?): VirtualFile? {
    if (editor == null && file != null) {
      if (file.isDirectory) return file
      return file.parent
    }
    return null
  }

  private fun updateList(list: JBList<Blueprint>, model: DefaultListModel<Blueprint>, project: Project, cli: VirtualFile) {
    list.setPaintBusy(true)
    model.clear()
    ApplicationManager.getApplication().executeOnPooledThread({
      val blueprints = BlueprintsLoader.load(project, cli)
      ApplicationManager.getApplication().invokeLater({
                                                        blueprints.forEach {
                                                          model.addElement(it)
                                                        }
                                                        list.setPaintBusy(false)
                                                      })
    })
  }

  private fun askOptions(project: Project, popup: JBPopup, blueprint: Blueprint, cli: VirtualFile, workingDir: VirtualFile?) {
    popup.closeOk(null)
    val dialog = object: DialogWrapper(project, true) {
      private lateinit var editor:EditorTextField
      init {
        title = "Generate ${blueprint.name}"
        init()
      }

      override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(JLabel(blueprint.description), BorderLayout.NORTH)
        editor = TextFieldWithAutoCompletion.create(project, blueprint.args, false, null)
        editor.setPreferredWidth(250)
        panel.add(LabeledComponent.create(editor, "Parameters"), BorderLayout.SOUTH)
        return panel
      }

      override fun getPreferredFocusedComponent(): JComponent {
        return editor
      }

      fun arguments():Array<String> {
        val tokenizer = CommandLineTokenizer(editor.text)
        val result:MutableList<String> = mutableListOf()
        while (tokenizer.hasMoreTokens()) {
          result.add(tokenizer.nextToken())
        }
        return result.toTypedArray()
      }
    }

    if (dialog.showAndGet()) {
      runGenerator(project, blueprint, dialog.arguments(), cli, workingDir)
    }
  }

  private fun runGenerator(project: Project, blueprint: Blueprint, arguments: Array<String>, cli: VirtualFile, workingDir: VirtualFile?) {
    val interpreter = NodeJsInterpreterManager.getInstance(project).default
    val node = NodeJsLocalInterpreter.tryCast(interpreter) ?: return

    val modules:MutableList<CompletionModuleInfo> = mutableListOf()
    NodeModuleSearchUtil.findModulesWithName(modules, AngularCLIProjectGenerator.PACKAGE_NAME, cli, false, node)

    val module = modules.firstOrNull() ?: return

    val filter = AngularCLIFilter(project, cli.path)
    AngularCLIProjectGenerator.generate(node, AngularCLIProjectGenerator.ng(module.virtualFile?.path!!),
                                        cli, VfsUtilCore.virtualToIoFile(workingDir ?: cli), project,
                                        null, arrayOf(filter), "generate", blueprint.name, *arguments)
  }

  override fun update(e: AnActionEvent?) {
    val project = e?.project
    val file = e?.getData(PlatformDataKeys.VIRTUAL_FILE)

    e?.presentation?.isEnabledAndVisible = project != null && findAngularCliFolder(project, file) != null
  }
}
