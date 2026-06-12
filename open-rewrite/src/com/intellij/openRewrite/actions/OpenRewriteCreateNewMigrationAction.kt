package com.intellij.openRewrite.actions

import com.intellij.ide.IdeView
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager

internal class OpenRewriteCreateNewMigrationAction : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = e.project != null && findDirectory() != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val directory = findDirectory() ?: return
    val psiDirectory = PsiManager.getInstance(project).findDirectory(directory) ?: return
    val ideView = object : IdeView {
      override fun getDirectories(): Array<PsiDirectory> = arrayOf(psiDirectory)
      override fun getOrChooseDirectory(): PsiDirectory = psiDirectory
    }
    val dataContext = SimpleDataContext.getSimpleContext(LangDataKeys.IDE_VIEW, ideView, e.dataContext)
    val wrappedEvent = AnActionEvent.createFromInputEvent(e.inputEvent, e.place, e.presentation, dataContext)
    OpenRewriteCreateRecipeFileAction().actionPerformed(wrappedEvent)
  }

  private fun findDirectory(): VirtualFile? {
    val rootPath = ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance())
    return LocalFileSystem.getInstance().findFileByPath(rootPath)?.takeIf { it.isDirectory }
  }
}