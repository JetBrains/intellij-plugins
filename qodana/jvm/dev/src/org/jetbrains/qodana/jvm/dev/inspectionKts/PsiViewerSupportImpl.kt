package org.jetbrains.qodana.jvm.dev.inspectionKts

import com.intellij.dev.psiViewer.PsiViewerDialog
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.inspectionKts.ui.PsiViewerSupport

private class PsiViewerSupportImpl : PsiViewerSupport {
  override suspend fun openPsiViewerDialog(project: Project, editor: Editor) {
    withContext(QodanaDispatchers.Ui) {
      PsiViewerDialog(project, editor).show()
    }
  }
}