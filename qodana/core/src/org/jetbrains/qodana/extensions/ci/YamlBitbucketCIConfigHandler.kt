package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.qodana.extensions.createKeyValue
import org.jetbrains.qodana.extensions.createSequenceItemRaw
import org.jetbrains.qodana.extensions.findMappingOrAdd
import org.jetbrains.qodana.extensions.putSequenceItem
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence

class YamlBitbucketCIConfigHandler : BitbucketCIConfigHandler {
  override suspend fun addQodanaStepToBranches(project: Project, initialText: String, stepText: String, branches: List<String>): String? {
    return readAction {
      val file = PsiFileFactory.getInstance(project).createFileFromText("file", YAMLFileType.YML, initialText)
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction null
      val elementGenerator = YAMLElementGenerator.getInstance(project)
      val pipelines = findMappingOrAdd(project, file, elementGenerator, topMapping, "pipelines",
                                       "pipelines:\n  branches:\n" + branches.joinToString(separator = "\n", postfix = "\n") { "$it:\n$stepText" }.replaceIndent("    ")
      )?.value as? YAMLMapping ?: return@readAction file.text

      val branchesNode = findMappingOrAdd(project, file, elementGenerator,
                                          pipelines, "branches",
                                          "  branches:\n" + branches.joinToString(separator = "\n", postfix = "\n") { "$it:\n$stepText" }.replaceIndent("    ")
      )?.value as? YAMLMapping ?: return@readAction file.text

      branches.forEach {
        val branchNode = findMappingOrAdd(project, file, elementGenerator,
                                         branchesNode, it, "$it:\n$stepText\n".replaceIndent("    ")
        )?.value as? YAMLSequence ?: return@forEach
        val stepItem = elementGenerator.createSequenceItemRaw(stepText) ?: return@forEach
        branchNode.putSequenceItem(stepItem)
        CodeStyleManager.getInstance(project).adjustLineIndent(file, stepItem.textRange)
      }
      file.text
    }
  }

  override suspend fun addCachesSection(project: Project, initialText: String): String? {
    return readAction {
      val file = PsiFileFactory.getInstance(project).createFileFromText("file", YAMLFileType.YML, initialText)
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction null
      val elementGenerator = YAMLElementGenerator.getInstance(project)
      val definitions = findMappingOrAdd(project, file, elementGenerator, topMapping, "definitions", """
        definitions:
          caches:
            qodana: ~/.qodana/cache
      """.trimIndent())?.value as? YAMLMapping ?: return@readAction file.text
      val caches = findMappingOrAdd(project, file, elementGenerator, definitions, "caches", """
        caches:
          qodana: ~/.qodana/cache
      """.replaceIndent("  "))?.value as? YAMLMapping ?: return@readAction file.text
      val newNode = elementGenerator.createKeyValue("qodana: ~/.qodana/cache") ?: return@readAction null
      caches.putKeyValue(newNode)
      file.text
    }
  }

  override suspend fun isQodanaStepPresent(project: Project, virtualFile: VirtualFile): Boolean {
    return readAction {
      val file = PsiManager.getInstance(project).findFile(virtualFile)
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction false
      val pipelines = topMapping.getKeyValueByKey("pipelines")?.value as? YAMLMapping ?: return@readAction false
      val branches = pipelines.getKeyValueByKey("branches")?.value as? YAMLMapping ?: return@readAction false
      branches.keyValues.asSequence()
        .map { it.value as? YAMLSequence }
        .filterNotNull()
        .any { sequence ->
          sequence.items
            .map { it.value as YAMLMapping }
            .mapNotNull { it.getKeyValueByKey("step")?.value as? YAMLMapping }
            .any { it.getKeyValueByKey("script")?.value?.text?.contains("qodana") == true }
        }
    }
  }
}