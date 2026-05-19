package com.intellij.lang.javascript.linter.eslint

import com.intellij.ide.actionsOnSave.ActionsOnSaveConfigurable
import com.intellij.lang.javascript.linter.eslint.EslintBundle
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import javax.swing.JCheckBox
import javax.swing.JTextField

internal class EslintBottomContent {

  lateinit var runForFilesField: JTextField
  lateinit var runOnSaveCheckBox: JCheckBox
  private lateinit var runForFilesRow: Row

  @JvmField
  val panel = panel {
    runForFilesRow = row(EslintBundle.message("eslint.run.for.files.label")) {
      runForFilesField = textField()
        .align(AlignX.FILL)
        .comment(EslintBundle.message("eslint.files.pattern.comment"))
        .component
    }
    row {
      runOnSaveCheckBox = checkBox(EslintBundle.message("eslint.run.on.save"))
        .component
      cell(ActionsOnSaveConfigurable.createGoToActionsOnSavePageLink())
    }
  }

  fun setRunForFilesRowEnabled(enabled: Boolean) {
    runForFilesRow.enabled(enabled)
  }
}