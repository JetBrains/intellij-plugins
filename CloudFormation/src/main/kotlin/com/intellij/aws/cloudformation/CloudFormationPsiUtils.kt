package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnFunctionNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
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
    val baseElement = parser.getPsiElement(node)

    // handle corner case with !Ref "xxxx" or !Sub "yyy"
    if (node is CfnScalarValueNode) {
      val baseNodes = parser.getCfnNodes(baseElement).filter { it != node }
      if (baseNodes.isNotEmpty()) {
        val otherNode = baseNodes.single()
        assert(otherNode is CfnFunctionNode)
        return otherNode
      }
    }

    var element = baseElement.parent
    while (element != null) {
      val parentNodes = parser.getCfnNodes(element)
      if (parentNodes.size > 1) {
        error("Multiple matches while searching for parent of $node: " + parentNodes.joinToString())
      }
      if (parentNodes.size == 1) return parentNodes.single()

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
