// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.actions

import com.intellij.CommonBundle
import com.intellij.execution.configurations.CommandLineTokenizer
import com.intellij.icons.AllIcons
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.util.NodePackage
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
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.speedSearch.ListWithFilter
import com.intellij.util.Function
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.JBScalableIcon
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.angular2.cli.*
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CLI_PACKAGE
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel

class AngularCliGenerateAction : DumbAwareAction() {

  var reloadingList: Boolean = false

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
    val editor = e.getData(PlatformDataKeys.FILE_EDITOR)
    val cli = AngularCliUtil.findAngularCliFolder(project, file) ?: return

    if (!AngularCliUtil.hasAngularCLIPackageInstalled(project, cli)) {
      AngularCliUtil.notifyAngularCliNotInstalled(project, cli, Angular2Bundle.message("angular.action.ng-generate.cant-generate-code"))
      return
    }

    val model = SortedListModel<Schematic>(Comparator.comparing { b1: Schematic ->
      when {
        b1.error != null -> 2
        b1.name!!.contains(":") -> 1
        else -> 0
      }
    }.thenComparing { b1: Schematic -> b1.name!! })
    val list = JBList<Schematic>(model)
    updateList(list, model, project, cli)
    list.cellRenderer = object : ColoredListCellRenderer<Schematic>() {
      override fun customizeCellRenderer(list: JList<out Schematic>,
                                         value: Schematic,
                                         index: Int,
                                         selected: Boolean,
                                         hasFocus: Boolean) {
        if (!selected && index % 2 == 0) {
          background = UIUtil.getDecoratedRowColor()
        }
        icon = JBUIScale.scaleIcon(EmptyIcon.create(5) as JBScalableIcon)
        if (value.error != null) {
          append(value.name!!, SimpleTextAttributes.ERROR_ATTRIBUTES, true)
          append(Angular2Bundle.message("angular.action.ng-generate.error-label", value.error!!.decapitalize()),
                 SimpleTextAttributes.GRAY_ATTRIBUTES, false)
        }
        else {
          append(value.name!!, SimpleTextAttributes.REGULAR_ATTRIBUTES, true)
          if (value.description != null) {
            append(" - " + value.description!!, SimpleTextAttributes.GRAY_ATTRIBUTES, false)
          }
        }
      }
    }

    val actionGroup = DefaultActionGroup()
    val refresh: AnAction = object : AnAction(AllIcons.Actions.Refresh) {
      init {
        shortcutSet = CustomShortcutSet(*KeymapManager.getInstance().activeKeymap.getShortcuts("Refresh"))
      }

      override fun actionPerformed(e: AnActionEvent) {
        AngularCliSchematicsRegistryService.getInstance().clearProjectSchematicsCache()
        updateList(list, model, project, cli)
      }

      override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = !reloadingList
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
    val pane = ListWithFilter.wrap(list, scroll) { obj: Schematic? -> obj.toString() }

    val builder = JBPopupFactory
      .getInstance()
      .createComponentPopupBuilder(pane, list)
      .setMayBeParent(true)
      .setRequestFocus(true)
      .setFocusable(true)
      .setFocusOwners(arrayOf<Component>(list))
      .setLocateWithinScreenBounds(true)
      .setCancelOnOtherWindowOpen(true)
      .setMovable(true)
      .setResizable(true)
      .setTitle(Angular2Bundle.message("angular.action.ng-generate.title"))
      .setSettingButtons(toolbarComponent)
      .setCancelOnWindowDeactivation(false)
      .setCancelOnClickOutside(true)
      .setDimensionServiceKey(project, "org.angular.cli.generate", true)
      .setMinSize(Dimension(JBUI.scale(350), JBUI.scale(300)))
      .setCancelButton(IconButton(CommonBundle.message("action.text.close"), AllIcons.Actions.Close, AllIcons.Actions.CloseHovered))
    val popup = builder.createPopup()
    list.addKeyListener(object : KeyAdapter() {
      override fun keyPressed(e: KeyEvent?) {
        if (list.selectedValue == null) return
        if (e?.keyCode == KeyEvent.VK_ENTER) {
          e.consume()
          askOptions(project, popup, list.selectedValue as Schematic, cli, workingDir(editor, file))
        }
      }
    })
    object : DoubleClickListener() {
      override fun onDoubleClick(event: MouseEvent): Boolean {
        if (list.selectedValue == null) return true
        askOptions(project, popup, list.selectedValue as Schematic, cli, workingDir(editor, file))
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

  private fun updateList(list: JBList<Schematic>, model: SortedListModel<Schematic>, project: Project, cli: VirtualFile) {
    list.setPaintBusy(true)
    reloadingList = true
    model.clear()
    ApplicationManager.getApplication().executeOnPooledThread {
      val schematics = AngularCliSchematicsRegistryService.getInstance().getSchematics(project, cli)
      ApplicationManager.getApplication().invokeLater {
        model.clear()
        schematics.forEach {
          model.add(it)
        }
        reloadingList = false
        list.setPaintBusy(false)
      }
    }
  }

  private fun askOptions(project: Project, popup: JBPopup, schematic: Schematic, cli: VirtualFile, workingDir: VirtualFile?) {
    if (schematic.error != null) {
      return
    }
    popup.closeOk(null)
    val dialog = object : DialogWrapper(project, true) {
      private lateinit var editor: EditorTextField

      init {
        title = Angular2Bundle.message("action.angularCliGenerate.title", schematic.name)
        init()
      }

      override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 4))
        panel.add(JLabel(schematic.description), BorderLayout.NORTH)
        editor = SchematicOptionsTextField(project, schematic.options)
        editor.setPreferredWidth(250)
        panel.add(LabeledComponent.create(
          editor, Angular2Bundle.message("angular.action.ng-generate.label.parameters", paramsDesc(schematic))), BorderLayout.SOUTH)
        return panel
      }

      override fun getPreferredFocusedComponent(): JComponent {
        return editor
      }

      fun paramsDesc(b: Schematic): String {
        val argDisplay = b.arguments.joinToString(" ") { "<" + it.name + ">" }
        val optionsDisplay = if (b.options.isEmpty()) "" else Angular2Bundle.message("angular.action.ng-generate.params.options")

        val display = listOf(argDisplay, optionsDisplay).filter { it.isNotEmpty() }
        if (display.isEmpty()) {
          return ""
        }
        return display.joinToString(" ", " (", ")")
      }

      fun arguments(): Array<String> {
        val tokenizer = CommandLineTokenizer(editor.text)
        val result: MutableList<String> = mutableListOf()
        while (tokenizer.hasMoreTokens()) {
          result.add(tokenizer.nextToken())
        }
        return result.toTypedArray()
      }
    }

    if (dialog.showAndGet()) {
      runGenerator(project, schematic, dialog.arguments(), cli, workingDir)
    }
  }

  private fun runGenerator(project: Project, schematic: Schematic, arguments: Array<String>, cli: VirtualFile, workingDir: VirtualFile?) {
    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter ?: return

    val modules: MutableList<CompletionModuleInfo> = mutableListOf()
    NodeModuleSearchUtil.findModulesWithName(modules, ANGULAR_CLI_PACKAGE, cli, null)

    val module = modules.firstOrNull() ?: return

    val filter = AngularCliFilter(project, cli.path)
    AngularCliProjectGenerator.generate(interpreter, NodePackage(module.virtualFile?.path!!),
                                        Function { pkg -> pkg.findBinFile("ng", null)?.absolutePath },
                                        cli, VfsUtilCore.virtualToIoFile(workingDir ?: cli), project,
                                        null, arrayOf(filter), "generate", schematic.name, *arguments)
  }

  override fun update(e: AnActionEvent) {
    val project = e.project
    val file = e.getData(PlatformDataKeys.VIRTUAL_FILE)

    e.presentation.isEnabledAndVisible = project != null
                                         && file != null
                                         && AngularCliUtil.findAngularCliFolder(project, file) != null
  }

}
