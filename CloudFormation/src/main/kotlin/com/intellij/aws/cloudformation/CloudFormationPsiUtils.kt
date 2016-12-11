package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

object CloudFormationPsiUtils {
  fun isCloudFormationFile(element: PsiElement): Boolean =
      element.containingFile.fileType === CloudFormationFileType.INSTANCE

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

}
