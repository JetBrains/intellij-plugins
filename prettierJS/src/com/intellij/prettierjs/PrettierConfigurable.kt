// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.*
import javax.swing.JLabel

class PrettierConfigurable(private val project: Project) : BoundSearchableConfigurable(
  PrettierBundle.message("configurable.PrettierConfigurable.display.name"), "procedures.prettier", "settings.javascript.prettier") {

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
      }

      row {
        val runOnSaveCheckBox = checkBox(PrettierBundle.message("run.on.save"),
                                         { prettierConfiguration.isRunOnSave },
                                         { prettierConfiguration.isRunOnSave = it }).component

        val filePatternsLabel = JLabel(PrettierBundle.message("files.pattern"))
        row(filePatternsLabel) {
          textField({ prettierConfiguration.filesPattern }, { prettierConfiguration.filesPattern = it })
            .comment(PrettierBundle.message("files.pattern.comment"))
            .component.also { filePatternsLabel.labelFor = it }
        }.enableIf(runOnSaveCheckBox.selected)
      }
    }
  }
}