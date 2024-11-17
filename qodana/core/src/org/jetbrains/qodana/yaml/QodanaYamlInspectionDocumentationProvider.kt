package org.jetbrains.qodana.yaml

import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.Nls

class QodanaYamlInspectionDocumentationProvider : PsiDocumentationTargetProvider {
  override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
    if (element.containingFile == null || !isQodanaYaml(element.containingFile)) return null

    return getInspectionFromElement(element)
      ?.displayName
      ?.let(::QodanaYamlInspectionDocumentationTarget)
  }
}

internal class QodanaYamlInspectionDocumentationTarget(val text: @Nls String) : DocumentationTarget {
  override fun createPointer(): Pointer<out DocumentationTarget> = Pointer { this }

  override fun computePresentation(): TargetPresentation {
    return TargetPresentation
      .builder(text)
      .presentation()
  }

  override fun computeDocumentation(): DocumentationResult? {
    return DocumentationResult.documentation(text)
  }

  override fun computeDocumentationHint(): String = text
}