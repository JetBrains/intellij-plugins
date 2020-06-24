// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.*
import java.nio.file.FileSystems
import java.util.regex.PatternSyntaxException
import javax.swing.JLabel

class PrettierConfigurable(private val project: Project) : BoundSearchableConfigurable(
  PrettierBundle.message("configurable.PrettierConfigurable.display.name"), "reference.settings.prettier", "settings.javascript.prettier") {

  override fun createPanel(): DialogPanel {
    val prettierConfiguration = PrettierConfiguration.getInstance(project)

    return panel {
      val nodeInterpreterField = NodeJsInterpreterField(project, false)
      row(JLabel(NodeJsInterpreterField.getLabelTextForComponent()).apply { labelFor = nodeInterpreterField }) {
        nodeInterpreterField().withBinding(
          { it.interpreterRef },
          { nodeJsInterpreterField, interpreterRef -> nodeJsInterpreterField.interpreterRef = interpreterRef },
          PropertyBinding({ prettierConfiguration.interpreterRef }, { prettierConfiguration.withInterpreterRef(it) })
        )
      }

      val packageField = NodePackageField(nodeInterpreterField, PrettierUtil.PACKAGE_NAME)
      row(JLabel(PrettierBundle.message("prettier.package.label")).apply { labelFor = packageField }) {
        packageField().withBinding(
          { it.selectedRef },
          { nodePackageField, nodePackageRef -> nodePackageField.selectedRef = nodePackageRef },
          PropertyBinding({ prettierConfiguration.nodePackageRef }, { prettierConfiguration.withLinterPackage(it) })
        )
      }.largeGapAfter()

      row {
        val runForFilesLabel = JLabel(PrettierBundle.message("run.for.files.label"))
        runForFilesLabel()

        cell(isFullWidth = true) {
          textField({ prettierConfiguration.filesPattern }, { prettierConfiguration.filesPattern = it })
            .withValidationOnInput {
              try {
                FileSystems.getDefault().getPathMatcher("glob:" + it.text)
                null
              }
              catch (e: PatternSyntaxException) {
                ValidationInfo(e.message?.lines()?.firstOrNull() ?: PrettierBundle.message("invalid.pattern"), it)
              }
            }
            .component.apply { runForFilesLabel.labelFor = this }

          comment(PrettierBundle.message("files.pattern.comment"))
        }
      }

      // empty label - to have the check box below the file patterns field
      row("") {
        cell {
          checkBox(PrettierBundle.message("on.code.reformat.label"),
                   { prettierConfiguration.isRunOnReformat },
                   { prettierConfiguration.isRunOnReformat = it })

          val shortcut = ActionManager.getInstance().getKeyboardShortcut(IdeActions.ACTION_EDITOR_REFORMAT)
          shortcut?.let { comment(KeymapUtil.getShortcutText(it)) }
        }
      }

      // empty label - to have the check box below the file patterns field
      row("") {
        cell {
          checkBox(PrettierBundle.message("on.save.label"),
                   { prettierConfiguration.isRunOnSave },
                   { prettierConfiguration.isRunOnSave = it })

          val shortcut = ActionManager.getInstance().getKeyboardShortcut("SaveAll")
          shortcut?.let { comment(KeymapUtil.getShortcutText(it)) }
        }
      }
    }
  }
}