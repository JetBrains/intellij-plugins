package com.intellij.aws.cloudformation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl

internal class CloudFormationYamlAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is YAMLPlainTextImpl && element.parent is YAMLKeyValue) {
      val keyValue = element.parent as YAMLKeyValue
      if (keyValue.keyText == "Type") {
        val resourcesParent = (keyValue.parentMapping?.parent as? YAMLKeyValue)?.parentMapping?.parent
        if (resourcesParent is YAMLKeyValue && resourcesParent.keyText == "Resources") {
          holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .textAttributes(AWS_RESOURCE_TYPE)
            .create()
        }
      }
    }
  }

  companion object {
    val AWS_RESOURCE_TYPE: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("AWS.RESOURCE.TYPE", DefaultLanguageHighlighterColors.METADATA)
  }
}