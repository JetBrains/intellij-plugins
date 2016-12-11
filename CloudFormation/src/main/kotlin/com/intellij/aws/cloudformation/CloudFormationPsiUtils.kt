package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNameNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnPropertiesNode
import com.intellij.aws.cloudformation.model.CfnPropertyNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
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

  class ResourceTypeValueMatch(val name: CfnNameNode, val resource: CfnResourceNode) {
    companion object {
      fun match(position: PsiElement, parsed: CloudFormationParsedFile): ResourceTypeValueMatch? {
        val nameNode = parsed.getCfnNode(position) as? CfnNameNode ?: return null

        val parent = getParent(nameNode, parsed)
        if (parent is CfnResourceNode && parent.type == nameNode) {
          return ResourceTypeValueMatch(nameNode, parent)
        }

        return null
      }
    }
  }

  class ResourcePropertyNameMatch(val name: CfnNameNode,
                                  val property: CfnPropertyNode,
                                  val properties: CfnPropertiesNode,
                                  val resource: CfnResourceNode) {
    companion object {
      fun match(position: PsiElement, parsed: CloudFormationParsedFile): ResourcePropertyNameMatch? {
        val nameNode = parsed.getCfnNode(position) as? CfnNameNode ?: return null

        val propertyNode = getParent(nameNode, parsed) as? CfnPropertyNode ?: return null
        val propertiesNode = getParent(propertyNode, parsed) as? CfnPropertiesNode ?: return null
        val resourceNode = getParent(propertiesNode, parsed) as? CfnResourceNode ?: return null

        if (propertyNode.name == nameNode) {
          return ResourcePropertyNameMatch(nameNode, propertyNode, propertiesNode, resourceNode)
        }

        return null
      }
    }
  }
}
