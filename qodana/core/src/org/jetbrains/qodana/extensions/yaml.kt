package org.jetbrains.qodana.extensions

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.*

internal fun getOrAddSequenceForMapping(elementGenerator: YAMLElementGenerator, mapping: YAMLMapping, name: String): YAMLValue? {
  if (mapping.getKeyValueByKey(name) == null) {
    val newNode = elementGenerator.createYamlKeyValue(name, "_")
    mapping.putKeyValue(newNode)
  }

  val node = mapping.getKeyValueByKey(name) ?: return null
  if (node.value == null || node.value !is YAMLSequence) {
    val emptySequence = elementGenerator.createEmptySequence()
    node.setValue(emptySequence)
  }
  return node.value
}

internal fun YAMLSequence.putSequenceItem(itemToAdd: YAMLSequenceItem) {
  val lastChild = lastChild
  if (lastChild != null && lastChild.node.elementType != YAMLTokenTypes.EOL) {
    node.addChild(YAMLElementGenerator.getInstance(project).createEol().node)
  }
  val indent = if (lastChild != null) YAMLUtil.getIndentToThisElement(lastChild) else YAMLUtil.getIndentToThisElement(this) + 2
  node.addChild(YAMLElementGenerator.getInstance(project).createIndent(indent).node)
  node.addChild(itemToAdd.node)
}

internal fun findMappingOrAdd(
  project: Project,
  file: PsiFile,
  elementGenerator: YAMLElementGenerator,
  mappingToSearch: YAMLMapping,
  mappingName: String,
  content: String
): YAMLKeyValue? {
  val mapping = mappingToSearch.getKeyValueByKey(mappingName)
  if (mapping == null) {
    val toAdd = elementGenerator.createKeyValue(content) ?: return null
    mappingToSearch.putKeyValue(toAdd)
    CodeStyleManager.getInstance(project).adjustLineIndent(file, mappingToSearch.textRange)
    return null
  }
  return mappingToSearch.getKeyValueByKey(mappingName)
}

internal fun YAMLElementGenerator.createMappingWithContent(name: String, content: String): YAMLKeyValue? {
  val mappingText = "$name:\n${content.replaceIndent("  ")}"
  return PsiTreeUtil.findChildOfType(createDummyYamlWithText(mappingText.trimIndent()), YAMLKeyValue::class.java)
}

internal fun YAMLElementGenerator.createKeyValue(content: String): YAMLKeyValue? {
  return PsiTreeUtil.findChildOfType(createDummyYamlWithText(content), YAMLKeyValue::class.java)
}

internal fun YAMLElementGenerator.createSequenceItem(data: String?): YAMLSequenceItem? {
  return PsiTreeUtil.findChildOfType(createDummyYamlWithText("- $data"), YAMLSequenceItem::class.java)
}

internal fun YAMLElementGenerator.createSequenceItemRaw(data: String): YAMLSequenceItem? {
  return PsiTreeUtil.findChildOfType(createDummyYamlWithText(data), YAMLSequenceItem::class.java)
}