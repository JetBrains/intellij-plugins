package com.intellij.dts.inspections

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.dts.lang.DtsFileType
import com.intellij.dts.settings.DtsSettings
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DtsInspectionsRestarter(
  private val project: Project,
  private val parentScope: CoroutineScope,
) : DtsSettings.ChangeListener {

  override fun settingsChanged(settings: DtsSettings) {
    parentScope.launch(Dispatchers.EDT) {
      val codeAnalyzer = DaemonCodeAnalyzer.getInstance(project)
      val psiManger = PsiManager.getInstance(project)
      val fileEditor = FileEditorManager.getInstance(project)

      edtWriteAction {
        fileEditor.allEditors
          .map { it.file }
          .filter { it.fileType == DtsFileType }
          .mapNotNull { psiManger.findFile(it) }
          .forEach { codeAnalyzer.restart(it, this) }
      }
    }
  }
}