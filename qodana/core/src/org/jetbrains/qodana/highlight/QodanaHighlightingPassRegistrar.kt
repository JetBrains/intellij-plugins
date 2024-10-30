package org.jetbrains.qodana.highlight

import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactoryRegistrar
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.extensions.QodanaHighlightingSupportInfoProvider

internal class QodanaHighlightingPassRegistrar : TextEditorHighlightingPassFactory, TextEditorHighlightingPassFactoryRegistrar, DumbAware {
  override fun registerHighlightingPassFactory(registrar: TextEditorHighlightingPassRegistrar, project: Project) {
    registrar.registerTextEditorHighlightingPass(
      this,
      QodanaHighlightingSupportInfoProvider.getPrecedingPassesIds().toIntArray(),
      null,
      false,
      -1
    )
  }

  override fun createHighlightingPass(psiFile: PsiFile, editor: Editor): TextEditorHighlightingPass? {
    // performance: do not load state if not yet loaded !
    val highlightedReportService = QodanaHighlightedReportService.getInstanceIfCreated(psiFile.project) ?: return null
    val passState = QodanaHighlightingPassState.getOrCreateForEditor(psiFile.project, editor, highlightedReportService, psiFile) ?: return null
    return QodanaReportHighlightingPass(psiFile, editor, highlightedReportService, passState)
  }
}