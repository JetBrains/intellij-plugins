package com.intellij.dts.inspections

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.dts.lang.DtsFileType
import com.intellij.dts.settings.DtsSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager

class DtsInspectionsRestarter(private val project: Project) : DtsSettings.ChangeListener {
  override fun settingsChanged(settings: DtsSettings) = ApplicationManager.getApplication().runWriteAction {
    val codeAnalyzer = DaemonCodeAnalyzer.getInstance(project)
    val psiManger = PsiManager.getInstance(project)
    val fileEditor = FileEditorManager.getInstance(project)

    fileEditor.allEditors
      .map { it.file }
      .filter { it.fileType == DtsFileType }
      .mapNotNull { psiManger.findFile(it) }
      .forEach { codeAnalyzer.restart(it) }
  }
}