package org.jetbrains.qodana.yaml

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLScalar

class QodanaYamlInspectionHintProvider : InlayHintsProvider {
  companion object {
    const val PROVIDER_ID: String = "qodana.yaml.inspection.descriptions"
  }

  override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
    if (!isQodanaYaml(file)) return null
    return InspectionHintCollector()
  }

  private class InspectionHintCollector : SharedBypassCollector {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
      if (element !is YAMLScalar) return

      val description = getInspectionFromElement(element, fromParent = false)
        ?.groupDisplayPath()

      if (description != null) {
        sink.addPresentation(InlineInlayPosition(element.textRange.endOffset, true), hasBackground = true) {
          text(" $description")
        }
      }
    }
  }
}