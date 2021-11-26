package com.intellij.protobuf.lang.intentions

import com.intellij.openapi.command.undo.DocumentReference
import com.intellij.openapi.command.undo.DocumentReferenceManager
import com.intellij.openapi.command.undo.UndoableAction
import com.intellij.openapi.project.Project
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.protobuf.lang.util.ImportPathData

internal class PbAddImportPathUndoableAction(pathToAdd: ImportPathData, private val project: Project) : UndoableAction {
  private val currentDocumentReference = DocumentReferenceManager.getInstance().create(pathToAdd.originalPbVirtualFile)
  private val importPath = PbProjectSettings.ImportPathEntry(pathToAdd.effectiveImportPathUrl, "")

  override fun undo() {
    if (project.isDisposed) return
    val projectSettings = PbProjectSettings.getInstance(project)
    projectSettings.importPathEntries = projectSettings.importPathEntries.filter { it != importPath }
    PbProjectSettings.notifyUpdated(project)
  }

  override fun redo() {
    if (project.isDisposed) return
    val projectSettings = PbProjectSettings.getInstance(project)
    projectSettings.importPathEntries = listOf(*projectSettings.importPathEntries.toTypedArray(), importPath)
    PbProjectSettings.notifyUpdated(project)
  }

  override fun getAffectedDocuments(): Array<DocumentReference> {
    return arrayOf(currentDocumentReference)
  }

  override fun isGlobal(): Boolean {
    return true
  }
}