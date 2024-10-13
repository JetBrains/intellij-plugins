package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence

class YamlGitHubCIConfigHandler : GitHubCIConfigHandler {
  override suspend fun isQodanaJobPresent(project: Project, virtualFile: VirtualFile): Boolean {
    return readAction {
      val githubFile = PsiManager.getInstance(project).findFile(virtualFile)
      val topMapping = (githubFile as? YAMLFile)?.documents?.get(0)?.topLevelValue as? YAMLMapping ?: return@readAction false
      val jobs = topMapping.getKeyValueByKey("jobs")?.value as? YAMLMapping ?: return@readAction false
      jobs.keyValues.any { job ->
        val value = job.value as? YAMLMapping ?: return@any false
        val steps = value.getKeyValueByKey("steps")?.value as? YAMLSequence ?: return@any false
        steps.items.any innerAny@{
          val mapping = it.value as? YAMLMapping ?: return@innerAny false
          mapping.getKeyValueByKey("uses")?.value?.text?.contains("JetBrains/qodana-action") == true
        }
      }
    }
  }
}