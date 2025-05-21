package org.jetbrains.qodana.settings

import com.intellij.openapi.project.Project

open class DefaultQodanaYamlBuilder(private val project: Project): QodanaYamlBuilder() {
  override suspend fun getYamlItems(): List<QodanaYamlItem> {
    return QodanaYamlItemProvider.provideAll(project, DefaultQodanaItemContext())
  }
}