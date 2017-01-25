package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.json.psi.JsonObject
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightPlatformCodeInsightTestCase

object CloudFormationPsiUtils {
  fun isCloudFormationFile(element: PsiElement): Boolean =
      element.containingFile.fileType === JsonCloudFormationFileType.INSTANCE ||
      element.containingFile.fileType === YamlCloudFormationFileType.INSTANCE

  fun getRootExpression(file: PsiFile): JsonObject? {
    var cur: PsiElement? = file.firstChild
    while (cur != null) {
      if (cur is JsonObject) {
        return cur
      }
      cur = cur.nextSibling
    }

    return null
  }

  fun getObjectLiteralExpressionChild(parent: JsonObject?, childName: String): JsonObject? {
    val property = parent?.findProperty(childName) ?: return null
    return property.value as? JsonObject
  }

  fun getParent(node: CfnNode, parser: CloudFormationParsedFile): CfnNode? {
    var element = parser.getPsiElement(node).parent
    while (element != null) {
      val parentNode = parser.getCfnNode(element)
      if (parentNode != null) return parentNode

      element = element.parent
    }

    return null
  }

  fun getLineNumber(psiElement: PsiElement): Int {
    if (!psiElement.isValid) return -1
    //LightPlatformCodeInsightTestCase.assertTrue(psiElement.isPhysical)
    val manager = InjectedLanguageManager.getInstance(psiElement.project)
    val containingFile = manager.getTopLevelFile(psiElement)
    val document = PsiDocumentManager.getInstance(psiElement.project).getDocument(containingFile) ?: return -1
    var textRange = psiElement.textRange ?: return -1
    textRange = manager.injectedToHost(psiElement, textRange)
    val startOffset = textRange.startOffset
    val textLength = document.textLength
    LightPlatformCodeInsightTestCase.assertTrue(" at $startOffset, $textLength", startOffset <= textLength)
    return document.getLineNumber(startOffset) + 1
  }
}
