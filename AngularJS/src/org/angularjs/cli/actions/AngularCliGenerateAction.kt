package org.angularjs.cli.actions

import com.intellij.execution.configurations.CommandLineTokenizer
import com.intellij.icons.AllIcons
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.NodeSettings
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.speedSearch.ListWithFilter
import com.intellij.util.ui.JBUI
import icons.JavaScriptLanguageIcons
import org.angularjs.cli.AngularCLIProjectGenerator
import org.angularjs.cli.Blueprint
import org.angularjs.cli.BlueprintsLoader
import org.angularjs.index.AngularIndexUtil
import java.awt.Component
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JComponent

/**
 * @author Dennis.Ushakov
 */
class AngularCliGenerateAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return

    val model = DefaultListModel<Blueprint>()
    val list = JBList(model)
    updateList(list, model, project)

    val actionGroup = DefaultActionGroup()
    val refresh: AnAction = object : AnAction(JavaScriptLanguageIcons.BuildTools.Refresh) {
      init {
        shortcutSet = CustomShortcutSet(*KeymapManager.getInstance().activeKeymap.getShortcuts("Refresh"))
      }

      override fun actionPerformed(e: AnActionEvent?) {
        BlueprintsLoader.CacheModificationTracker.count++
        updateList(list, model, project)
      }
    }
    refresh.registerCustomShortcutSet(refresh.shortcutSet, list)
    actionGroup.addAction(refresh)

    val actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, true)
    actionToolbar.setReservePlaceAutoPopupIcon(false)
    actionToolbar.setMinimumButtonSize(Dimension(22, 22))
    val toolbarComponent = actionToolbar.component
    toolbarComponent.isOpaque = false

    val scroll = ScrollPaneFactory.createScrollPane(list)
    scroll.border = IdeBorderFactory.createEmptyBorder()
    val pane = ListWithFilter.wrap(list, scroll, StringUtil.createToStringFunction(Any::class.java))

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
        setCancelOnWindowDeactivation(true).
        setCancelOnClickOutside(true).
        setDimensionServiceKey(project, "org.angular.cli.generate", true).
        setMinSize(Dimension(JBUI.scale(200), JBUI.scale(200))).
        setCancelButton(IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered))
    val popup = builder.createPopup()
    list.addKeyListener(object: KeyAdapter() {
      override fun keyPressed(e: KeyEvent?) {
        if (e?.keyCode == KeyEvent.VK_ENTER) {
          e?.consume()
          askOptions(project, popup, list.selectedValue as Blueprint)
        }
      }
    })
    object: DoubleClickListener() {
      override fun onDoubleClick(event: MouseEvent?): Boolean {
        askOptions(project, popup, list.selectedValue as Blueprint)
        return true
      }
    }.installOn(list)
    popup.showCenteredInCurrentWindow(project)
  }

  private fun updateList(list: JBList, model: DefaultListModel<Blueprint>, project: Project) {
    list.setPaintBusy(true)
    model.clear()
    ApplicationManager.getApplication().executeOnPooledThread({

      val blueprints = BlueprintsLoader.load(project)
      ApplicationManager.getApplication().invokeLater({
                                                        blueprints.forEach {
                                                          model.addElement(it)
                                                        }
                                                        list.setPaintBusy(false)
                                                      })
    })
  }

  private fun askOptions(project: Project, popup: JBPopup, blueprint: Blueprint) {
    popup.closeOk(null)
    val dialog = object: DialogWrapper(project, true) {
      private lateinit var editor:EditorTextField
      init {
        title = "Generate ${blueprint.name}"
        init()
      }

      override fun createCenterPanel(): JComponent {
        editor = TextFieldWithAutoCompletion.create(project, blueprint.args, false, null)
        return LabeledComponent.create(editor, "Parameters")
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
      runGenerator(project, blueprint, dialog.arguments())
    }
  }

  private fun runGenerator(project: Project, blueprint: Blueprint, arguments: Array<String>) {
    val interpreter = NodeJsInterpreterManager.getInstance(project).default
    val node = NodeJsLocalInterpreter.tryCast(interpreter) ?: return

    val modules:MutableList<CompletionModuleInfo> = mutableListOf()
    val baseDir = project.baseDir
    NodeModuleSearchUtil.findModulesWithName(modules, "angular-cli", baseDir, NodeSettings.create(node), false)

    val module = modules.firstOrNull() ?: return

    AngularCLIProjectGenerator.generate(node, module.virtualFile?.path!!, project.baseDir, project, null, "generate", blueprint.name, *arguments)
  }

  override fun update(e: AnActionEvent?) {
    val project = e?.project
    e?.presentation?.isEnabledAndVisible = project != null && AngularIndexUtil.hasAngularJS2(project) &&
        project.baseDir.findChild("angular-cli.json") != null
  }
}
