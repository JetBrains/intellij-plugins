package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.qodana.extensions.*
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence

class YamlCircleCIConfigHandler : CircleCIConfigHandler {
  override suspend fun addOrb(project: Project, text: String, orbName: String, orbValue: String): String? {
    return readAction {
      val file = PsiFileFactory.getInstance(project).createFileFromText("file", YAMLFileType.YML, text)
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction null

      addSection(project, topMapping, "orbs", "$orbName: $orbValue")
      file.text
    }
  }

  override suspend fun addJob(project: Project, text: String, jobText: String): String? {
    return readAction {
      val file = PsiFileFactory.getInstance(project).createFileFromText("file", YAMLFileType.YML, text)
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction null

      addSection(project, topMapping, "jobs", jobText)
      file.text
    }
  }

  override suspend fun addWorkflowJob(project: Project, text: String, jobName: String, jobText: String): String? {
    return readAction {
      val file = PsiFileFactory.getInstance(project).createFileFromText("file", YAMLFileType.YML, text)
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction null
      val elementGenerator = YAMLElementGenerator.getInstance(project)

      val workflowsMapping =
        findMappingOrAdd(project, file, elementGenerator,
                         topMapping,"workflows",
                         "workflows:\n  $jobName:\n    jobs:\n${jobText.replaceIndent("      ")}\n"
        )?.value as? YAMLMapping ?: return@readAction file.text

      val jobMapping =
        findMappingOrAdd(project, file, elementGenerator,
                         workflowsMapping, jobName,
                         "$jobName:\n  jobs:\n${jobText.replaceIndent("    ")}\n".replaceIndent("  ")
      )?.value as? YAMLMapping ?: return@readAction file.text

      val jobs =
        findMappingOrAdd(project, file, elementGenerator,
                         jobMapping, "jobs",
                         "jobs:\n${jobText.replaceIndent("  ")}\n".replaceIndent("    ")
      ) ?: return@readAction file.text

      if (jobs.value == null || jobs.value !is YAMLSequence) return@readAction null
      val seq = jobs.value as? YAMLSequence ?: return@readAction null
      val itemToAdd = elementGenerator.createSequenceItemRaw(jobText.replaceIndent("      ")) ?: return@readAction null
      seq.putSequenceItem(itemToAdd)
      CodeStyleManager.getInstance(project).adjustLineIndent(file, itemToAdd.textRange)

      file.text
    }
  }

  override suspend fun isQodanaStepPresent(project: Project, virtualFile: VirtualFile): Boolean {
    return readAction {
      val file = PsiManager.getInstance(project).findFile(virtualFile)
      val topMapping = (file as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction false
      val jobs = topMapping.getKeyValueByKey("jobs")?.value as? YAMLMapping ?: return@readAction false
      jobs.keyValues.any { job ->
        val value = job.value as? YAMLMapping ?: return@any false
        val steps = value.getKeyValueByKey("steps")?.value as? YAMLSequence ?: return@any false
        steps.items.any { it.text.contains("qodana/scan") }
      }
    }
  }

  private fun addSection(project: Project, topMapping: YAMLMapping, name: String, content: String) {
    val elementGenerator = YAMLElementGenerator.getInstance(project)
    if (topMapping.getKeyValueByKey(name) == null) {
      val newNode = elementGenerator.createMappingWithContent(name, content) ?: return
      topMapping.putKeyValue(newNode)
      return
    }
    val orbs = topMapping.getKeyValueByKey(name) ?: return
    val orbsMapping = orbs.value as? YAMLMapping ?: return
    val indent = YAMLUtil.getIndentToThisElement(orbsMapping.lastChild)
    val toPut = elementGenerator.createKeyValue(content.replaceIndent(" ".repeat(indent))) ?: return
    orbsMapping.putKeyValue(toPut)
  }
}