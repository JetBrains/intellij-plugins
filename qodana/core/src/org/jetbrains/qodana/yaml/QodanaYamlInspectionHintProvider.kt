package org.jetbrains.qodana.yaml

import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.extensions.QodanaInspectionRetrievalLauncher
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
      // Here we need to wait for inspections from Rider
      if (!QodanaInspectionRetrievalLauncher.isInitialized()) {
        val project = element.project
        project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
          QodanaInspectionRetrievalLauncher.launchInspectionRetrieval(project)
        }
        return
      }

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