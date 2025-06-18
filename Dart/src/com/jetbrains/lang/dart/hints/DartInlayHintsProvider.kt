package com.jetbrains.lang.dart.hints

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService

class DartInlayHintsProvider : InlayHintsProvider {
  companion object {
    const val PROVIDER_ID: String = "dart.closing.labels"
  }

  override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector = object : OwnBypassCollector {
    override fun collectHintsForFile(file: PsiFile, sink: InlayTreeSink) {
      val virtualFile = file.virtualFile ?: return
      val project = file.project

      val analysisServerService = DartAnalysisServerService.getInstance(project)
      val closingLabels = analysisServerService.getClosingLabels(virtualFile)
      closingLabels.forEach {
        if (it.offset + it.length <= editor.document.textLength) {
          val line = editor.document.getLineNumber(it.offset + it.length)
          val position = EndOfLinePosition(line)

          sink.addPresentation(
            position = position,
            hintFormat = HintFormat.default) {
            text(it.label)
          }
        }
      }
    }
  }
}
