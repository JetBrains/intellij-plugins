package com.intellij.protobuf.lang.intentions

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.command.undo.UndoableAction
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.protobuf.ide.settings.PbLanguageSettingsConfigurable
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.util.ImportPathData
import javax.swing.Icon

internal sealed class PbImportIntentionVariant {
  abstract val icon: Icon

  abstract fun invokeAction(project: Project)

  object ManuallyConfigureImportPathsSettings : PbImportIntentionVariant() {
    val presentableName: String
      @NlsSafe
      get() = PbLangBundle.message("intention.manually.configure.imports")

    override val icon: Icon
      get() = AllIcons.General.Gear

    override fun invokeAction(project: Project) {
      invokeLater {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, PbLanguageSettingsConfigurable::class.java)
      }
    }
  }

  class AddImportPathToSettings(val importPathData: ImportPathData) : PbImportIntentionVariant() {
    override val icon: Icon
      get() = AllIcons.Actions.ModuleDirectory

    override fun invokeAction(project: Project) {
      WriteCommandAction.runWriteCommandAction(
        project,
        PbLangBundle.message("intention.add.import.path.popup.title"),
        PbLangBundle.message("intention.fix.import.problems.familyName"),
        {
          UndoManager.getInstance(project).undoableActionPerformed(
            PbAddImportPathUndoableAction(importPathData, project).also(UndoableAction::redo)
          )
        }
      )
    }
  }
}