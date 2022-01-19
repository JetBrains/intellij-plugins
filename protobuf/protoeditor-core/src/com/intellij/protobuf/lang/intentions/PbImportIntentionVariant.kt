package com.intellij.protobuf.lang.intentions

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.command.undo.UndoableAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.protobuf.ide.settings.PbLanguageSettingsConfigurable
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.intentions.util.ImportPathData
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.util.PbPsiFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
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

  open class AddImportPathToSettings(val importPathData: ImportPathData) : PbImportIntentionVariant() {
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

  class AddImportStatementAndPathToSettings(importPathData: ImportPathData) : AddImportPathToSettings(importPathData) {
    override fun invokeAction(project: Project) {
      val psiManager = project.service<PsiManager>()
      val (targetPsiFile, importedPsiFile) = runReadAction {
        val targetPsiFile = psiManager.findFile(importPathData.originalPbVirtualFile) ?: return@runReadAction null
        val importedPsiFile = psiManager.findFile(importPathData.importedPbVirtualFile) ?: return@runReadAction null
        targetPsiFile to importedPsiFile
      } ?: return

      // Important to use the same command groupId so that underlying write actions would be merged together
      WriteCommandAction.runWriteCommandAction(
        project,
        PbLangBundle.message("intention.add.import.path.popup.title"),
        PbLangBundle.message("intention.fix.import.problems.familyName"),
        {
          if (!addImportStatement(project, targetPsiFile, importedPsiFile)) return@runWriteCommandAction
          super.invokeAction(project)
        }
      )
    }

    private fun addImportStatement(project: Project, targetFile: PsiFile, protoFileToImport: PsiFile): Boolean {
      when {
        targetFile !is PbFile || protoFileToImport !is PbFile -> {
          thisLogger().warn(
            "Both target and imported files should be PROTO files, got target = '${targetFile.fileType}' and imported = '${protoFileToImport.fileType}' instead")
          return false
        }
        targetFile.importStatements.isNotEmpty() ->
          addImportAndNewLineAfter(project, targetFile, protoFileToImport, targetFile.importStatements.last(), true)

        targetFile.importStatements.isEmpty() && targetFile.syntaxStatement != null ->
          addImportAndNewLineAfter(project, targetFile, protoFileToImport, targetFile.syntaxStatement, true)

        targetFile.firstChild != null ->
          addImportAndNewLineAfter(project, targetFile, protoFileToImport, targetFile.firstChild, false)

        else ->
          addImportAndNewLineAfter(project, targetFile, protoFileToImport, null, false)
      }

      return true
    }

    private fun addImportAndNewLineAfter(project: Project,
                                         targetFile: PsiFile,
                                         importedFile: PsiFile,
                                         anchor: PsiElement?,
                                         afterAnchor: Boolean) {

      val importStatement = PbPsiFactory.createImportStatement(project, importedFile.name)
      val newLine = PbPsiFactory.createNewLine(project)

      val newImport =
        if (afterAnchor)
          targetFile.addAfter(importStatement, anchor)
        else
          targetFile.addBefore(importStatement, anchor)

      if (afterAnchor)
        targetFile.addBefore(newLine, newImport)
      else
        targetFile.addAfter(newLine, newImport)
    }
  }
}