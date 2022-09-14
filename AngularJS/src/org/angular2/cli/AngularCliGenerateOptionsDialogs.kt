// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.execution.configurations.CommandLineTokenizer
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.EditorTextField
import com.intellij.util.text.SemVer
import org.angular2.lang.Angular2Bundle
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class AngularCliGenerateOptionsDialogs(val project: Project, val schematic: Schematic, private val cliVersion: SemVer) :
  DialogWrapper(project, true) {
  private lateinit var editor: EditorTextField

  init {
    title = Angular2Bundle.message("action.angularCliGenerate.title", schematic.name)
    init()
  }

  override fun createCenterPanel(): JComponent {
    val panel = JPanel(BorderLayout(0, 4))
    panel.add(JLabel(schematic.description), BorderLayout.NORTH)
    editor = SchematicOptionsTextField(project, schematic.options, cliVersion)
    editor.setPreferredWidth(250)
    panel.add(LabeledComponent.create(
      editor, Angular2Bundle.message("angular.action.ng-generate.label.parameters", paramsDesc(schematic))), BorderLayout.SOUTH)
    return panel
  }

  override fun getPreferredFocusedComponent(): JComponent {
    return editor
  }

  private fun paramsDesc(b: Schematic): String {
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
