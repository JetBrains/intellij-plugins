package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnFunctionNode
import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.json.JsonFileType
import com.intellij.json.psi.JsonObject
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLPsiElement
import org.jetbrains.yaml.psi.YAMLValue
import org.jetbrains.yaml.psi.impl.YAMLCompoundValueImpl

fun CfnNode.parent(parser: CloudFormationParsedFile): CfnNode? {
  val baseElement = parser.getPsiElement(this)

  // handle corner case with !Ref "xxxx" or !Sub "yyy"
  // Second case:
  // Properties:
  //   Ty <-- here, it'll be both CfnResourcePropertyNode and CfnScalarValueNode
  //   A: B

  if (this is CfnScalarValueNode) {
    val baseNodes = parser.getCfnNodes(baseElement).filter { it !== this }
    if (baseNodes.isNotEmpty()) {
      val otherNode = baseNodes.single()
      assert(otherNode is CfnFunctionNode || otherNode is CfnNamedNode)
      return otherNode
    }
  }

  var element = baseElement.parent
  while (element != null) {
    val parentNodes = parser.getCfnNodes(element)
    if (parentNodes.size > 1) {
      error("Multiple matches while searching for parent of $this: " + parentNodes.joinToString())
    }
    if (parentNodes.size == 1) return parentNodes.single()

    element = element.parent
  }

  return null
}

inline fun <reified T> CfnNode.parentOfType(parser: CloudFormationParsedFile): T? {
  var current: CfnNode? = this
  while (current != null) {
    if (current is T) {
      return current
    }

    current = current.parent(parser)
  }

  return null
}

fun YAMLPsiElement.isYAMLCompoundValueImpl(): Boolean = this.javaClass == YAMLCompoundValueImpl::class.java
fun YAMLValue.getFirstTag() = when {
  this.isYAMLCompoundValueImpl() -> this.children.map { it as? YAMLValue }.firstOrNull()?.tag
  else -> this.tag
}

object CloudFormationPsiUtils {
  fun isCloudFormationFile(element: PsiElement): Boolean {
    val psiFile = element.containingFile

    val fileType = psiFile.viewProvider.fileType

    return when {
      fileType === JsonCloudFormationFileType.INSTANCE -> true
      fileType === YamlCloudFormationFileType.INSTANCE -> true
      fileType === JsonFileType.INSTANCE ->
        getRootExpression(psiFile)?.findProperty(CloudFormationSection.FormatVersion.id) != null
      fileType === YAMLFileType.YML -> {
        val yamlFile = psiFile as? YAMLFile
        val yamlDoc = yamlFile?.documents?.firstOrNull()
        val topLevelYamlMapping = yamlDoc?.topLevelValue as? YAMLMapping
        topLevelYamlMapping?.getKeyValueByKey(CloudFormationSection.FormatVersion.id) != null
      }
      else -> false
    }
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
