package com.intellij.lang.javascript.linter.eslint

import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSConfiguration
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSFixAction
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ESLintActionOnSave : ActionsOnSaveFileDocumentManagerListener.DocumentUpdatingActionOnSave() {
  override val presentableName: String = "ESLint"

  override fun isEnabledForProject(project: Project): Boolean =
    EslintConfiguration.getInstance(project).isFixOnSaveEnabled ||
    StandardJSConfiguration.getInstance(project).isFixOnSaveEnabled

  override suspend fun updateDocument(project: Project, document: Document) {
    val (action, psiFile) = readAction { getActionAndFileToProcess(project, document) } ?: return

    withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
      action.saveDocumentsIfNeeded()
    }

    val resultText = runEsLintAsActionOnSave(action, psiFile) ?: return

    writeCommandAction(project, JavaScriptBundle.message("javascript.linter.action.fix.problems.name", presentableName)) {
      EsLintFixAction.applyResultText(project, document, resultText)
    }
  }

  @RequiresReadLock
  private fun getActionAndFileToProcess(project: Project, document: Document): Pair<EsLintFixAction, PsiFile>? {
    val action = when {
      EslintConfiguration.getInstance(project).isFixOnSaveEnabled -> EsLintFixAction()
      StandardJSConfiguration.getInstance(project).isFixOnSaveEnabled -> StandardJSFixAction()
      else -> return null
    }
    val file = FileDocumentManager.getInstance().getFile(document) ?: return null
    if (!action.isEnabled(project, arrayOf(file)) || !action.isFileAccepted(project, file)) return null
    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null

    return action to psiFile
  }

  private fun runEsLintAsActionOnSave(action: EsLintFixAction, psiFile: PsiFile): String? {
    return try {
      action.fixFile(psiFile)
    }
    catch (e: LinterExecutionException) {
      thisLogger().info("Failed to run ESLint on save: $e")
      null
    }
  }
}
