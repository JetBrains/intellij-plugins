package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.json.psi.JsonObject
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightPlatformCodeInsightTestCase

object CloudFormationPsiUtils {
  fun isCloudFormationFile(element: PsiElement): Boolean {
    val fileType = element.containingFile.viewProvider.fileType
    return fileType === JsonCloudFormationFileType.INSTANCE || fileType === YamlCloudFormationFileType.INSTANCE
  }

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

  fun getParent(node: CfnNode, parser: CloudFormationParsedFile): CfnNode? {
    var element = parser.getPsiElement(node).parent
    while (element != null) {
      val parentNodes = parser.getCfnNodes(element).filter { it != node }
      if (parentNodes.size > 1) {
        error("Multiple matches while searching for parent of $node: " + parentNodes.joinToString())
      }
      if (parentNodes.size == 1) return parentNodes[0]

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
