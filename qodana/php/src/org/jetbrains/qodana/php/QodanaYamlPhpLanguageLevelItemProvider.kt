package org.jetbrains.qodana.php

import com.intellij.openapi.project.Project
import com.jetbrains.php.config.PhpLanguageLevel
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.settings.APPLIED_IN_CI_COMMENT
import org.jetbrains.qodana.settings.QodanaYamlItem
import org.jetbrains.qodana.settings.QodanaYamlItemProvider

class QodanaYamlPhpLanguageLevelItemProvider : QodanaYamlItemProvider {
  companion object {
    private const val ID = "php language level"
  }

  override suspend fun provide(project: Project): QodanaYamlItem? {
    val languageLevel = PhpProjectConfigurationFacade.getInstance(project).languageLevel
    if (languageLevel == PhpLanguageLevel.DEFAULT) return null
    @Language("PHP")
    val content = """
      
      php:
        version: ${languageLevel.versionString} #$APPLIED_IN_CI_COMMENT
    """.trimIndent()
    return QodanaYamlItem(ID, 125, content)
  }

}