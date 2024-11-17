package org.jetbrains.qodana.extensions

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.childrenOfType
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.psi.*
import org.jetbrains.yaml.psi.impl.YAMLBlockSequenceImpl

class QodanaYamlConfigUpdateHandler : ConfigUpdateHandler {
  override fun excludeFromConfig(project: Project, virtualFile: VirtualFile, inspectionId: String?, path: String?) {
    WriteCommandAction.runWriteCommandAction(project, null, null, {
      val file = PsiManager.getInstance(project).findFile(virtualFile) ?: return@runWriteCommandAction
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@runWriteCommandAction
      findOrInsertExcludeElement(project, topMapping, inspectionId, path)
    })
  }

  private fun findOrInsertExcludeElement(project: Project, topMapping: YAMLMapping, inspectionId: String?, path: String?) {
    val elementGenerator = YAMLElementGenerator.getInstance(project)

    val values = getOrAddSequenceForMapping(elementGenerator, topMapping, "exclude") ?: return
    if (values !is YAMLBlockSequenceImpl) return

    val mapping = getOrSetMappingWithPathsByName(elementGenerator, values, inspectionId) ?: return
    val paths = mapping.getKeyValueByKey("paths") ?: return

    if (path == null) {
      mapping.deleteKeyValue(paths)
      return
    }

    putPath(elementGenerator, paths, path)
  }

  private fun getOrSetMappingWithPathsByName(elementGenerator: YAMLElementGenerator, values: YAMLBlockSequenceImpl, inspectionId: String?): YAMLMapping? {
    val name = inspectionId ?: "All"
    val sequenceItems = values.items
      .filter { item -> item.keysValues.any { it.keyText == "name" && it.valueText == name } }

    val sequenceItem = if (sequenceItems.isEmpty()) {
      val generatedItem = elementGenerator.createSequenceItem("name: $name") ?: return null
      values.putSequenceItem(generatedItem)
      generatedItem
    } else {
      val generatedItem = sequenceItems.first() as YAMLSequenceItem
      if (generatedItem.childrenOfType<YAMLMapping>().first().getKeyValueByKey("paths") == null)
        return null
      generatedItem
    }

    val mapping = sequenceItem.childrenOfType<YAMLMapping>().first()
    if (mapping.getKeyValueByKey("paths") == null) {
      val ret = elementGenerator.createYamlKeyValue("paths", "_")
      mapping.putKeyValue(ret)
    }
    return mapping
  }

  private fun putPath(elementGenerator: YAMLElementGenerator, paths: YAMLKeyValue, path: String?) {
    if (paths.value !is YAMLSequence) {
      paths.setValue(elementGenerator.createEmptySequence())
    }
    val sequence = paths.value as YAMLSequence
    val item = elementGenerator.createSequenceItem(path) ?: return
    if (!sequence.items.any { it.value?.text == path })
      sequence.putSequenceItem(item)
  }
}
