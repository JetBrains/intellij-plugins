package org.jetbrains.qodana.jvm.java.ui

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.settings.QodanaYamlBuilder
import org.jetbrains.qodana.settings.QodanaYamlItem
import org.jetbrains.qodana.settings.QodanaYamlItemProvider
import org.jetbrains.qodana.settings.QodanaYamlLinterItemProvider

class GithubPromoQodanaYamlBuilder(val project: Project): QodanaYamlBuilder() {

  override suspend fun getYamlItems(): List<QodanaYamlItem> {
    return QodanaYamlItemProvider.Companion.provideAll(project,
                                                       GithubPromoQodanaYamlItemContext()).filter { it.id != QodanaYamlLinterItemProvider.Companion.ID }
  }
}