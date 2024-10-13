package org.jetbrains.qodana.settings

import com.intellij.openapi.project.Project
import org.jetbrains.annotations.VisibleForTesting

class DefaultQodanaYamlBuilder(private val project: Project) {
  suspend fun build(): String {
    val yamlItems = QodanaYamlItemProvider.provideAll(project)
    return build(yamlItems)
  }

  @VisibleForTesting
  fun build(items: List<QodanaYamlItem>): String {
    return items
      .sortedBy { it.weight }
      .distinctBy { it.id }
      .joinToString("\n") { it.content } + "\n"
  }
}