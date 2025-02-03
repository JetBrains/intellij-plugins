package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.qodana.extensions.getOrAddSequenceForMapping
import org.jetbrains.qodana.extensions.putSequenceItem
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence

class YamlAzureCIConfigHandler : AzureCIConfigHandler {
  override suspend fun insertStepToAzurePipelinesBuild(project: Project, initialText: String, taskToAddText: String): String? {
    return readAction {
      val file = PsiFileFactory.getInstance(project).createFileFromText("file", YAMLFileType.YML, initialText)
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction null
      val elementGenerator = YAMLElementGenerator.getInstance(project)
      val steps = getOrAddSequenceForMapping(elementGenerator, topMapping, "steps") as? YAMLSequence ?: return@readAction null
      val sequenceItem = elementGenerator.createSequenceItem(taskToAddText) ?: return@readAction null
      steps.putSequenceItem(sequenceItem)
      CodeStyleManager.getInstance(project).adjustLineIndent(file, sequenceItem.textRange)
      file.text
    }
  }

  override suspend fun isQodanaTaskPresent(project: Project, virtualFile: VirtualFile): Boolean {
    return readAction {
      val file = PsiManager.getInstance(project).findFile(virtualFile)
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction false
      val steps = topMapping.getKeyValueByKey("steps")?.value as? YAMLSequence ?: return@readAction false
      return@readAction steps.items.any { it.value?.text?.contains("QodanaScan") == true }
    }
  }
}