package org.jetbrains.qodana.inspectionKts.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface PsiViewerSupport {
  companion object {
    val EP_NAME = ExtensionPointName<PsiViewerSupport>("org.intellij.qodana.psiViewerSupport")
  }

  suspend fun openPsiViewerDialog(project: Project, editor: Editor)
}