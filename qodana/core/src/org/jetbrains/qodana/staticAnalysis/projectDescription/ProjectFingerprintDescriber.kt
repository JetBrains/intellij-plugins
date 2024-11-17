package org.jetbrains.qodana.staticAnalysis.projectDescription

import com.intellij.lang.Language
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex

class ProjectFingerprintDescriber : QodanaProjectDescriber {
  override val id: String = "ProjectFingerprint"

  override suspend fun description(project: Project): Any {
    val usedLanguages = mutableSetOf<Language>()

    readAction {
      ProjectFileIndex.getInstance(project).iterateContent { fileOrDir ->
        val language = LanguageUtil.getFileLanguage(fileOrDir)
        if (language != null) usedLanguages.add(language)
        true
      }
    }
    return ProjectFingerprint(usedLanguages.map { it.displayName }.filter { it.isNotEmpty() })
  }

  @Suppress("unused")
  class ProjectFingerprint(val languages: List<String>)
}
