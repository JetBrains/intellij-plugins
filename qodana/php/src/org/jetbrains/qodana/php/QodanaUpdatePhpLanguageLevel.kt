package org.jetbrains.qodana.php

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

private val LOG = logger<QodanaUpdatePhpLanguageLevel>()

class QodanaUpdatePhpLanguageLevel : QodanaWorkflowExtension {
  override suspend fun configureForQodana(config: QodanaConfig, project: Project) {
    val phpLanguageLevel = config.php
                             ?.version
                             ?.takeUnless(String::isBlank)
                             ?.toPhpLanguageLevel() ?: return

    LOG.info("Applying ${phpLanguageLevel.presentableName} as PHP Language Level")
    edtWriteAction {
      PhpProjectConfigurationFacade.getInstance(project).languageLevel = phpLanguageLevel
    }
  }
}
