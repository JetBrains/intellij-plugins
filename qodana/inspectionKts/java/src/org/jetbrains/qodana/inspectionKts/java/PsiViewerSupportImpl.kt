package org.jetbrains.qodana.inspectionKts.java

import com.intellij.dev.psiViewer.PsiViewerDialog
import com.intellij.openapi.application.UI
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.inspectionKts.ui.PsiViewerSupport

private class PsiViewerSupportImpl : PsiViewerSupport {
  override suspend fun openPsiViewerDialog(project: Project, editor: Editor) {
    withContext(Dispatchers.UI) {
      PsiViewerDialog(project, editor).show()
    }
  }
}