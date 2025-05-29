package org.jetbrains.qodana.jvm.java.ui

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.settings.QodanaYamlBuilder
import org.jetbrains.qodana.settings.QodanaYamlItem
import org.jetbrains.qodana.settings.QodanaYamlItemProvider
import org.jetbrains.qodana.settings.QodanaYamlLinterItemProvider

class GithubPromoQodanaYamlBuilder(val project: Project): QodanaYamlBuilder() {

  override suspend fun getYamlItems(): List<QodanaYamlItem> = QodanaYamlItemProvider.Companion
    .provideAll(project, GithubPromoQodanaYamlItemContext())
    // replaced by promo linter item
    .filter { it.id != QodanaYamlLinterItemProvider.Companion.ID }
}