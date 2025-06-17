package org.jetbrains.qodana.settings

import com.intellij.openapi.project.Project
import org.jetbrains.annotations.VisibleForTesting

class DefaultQodanaYamlBuilder(private val project: Project) {

  suspend fun build(context: DefaultQodanaYamlContext = DefaultQodanaYamlContext()): String {
    return build(getYamlItems(context))
  }

  private suspend fun getYamlItems(context: DefaultQodanaYamlContext): List<QodanaYamlItem> {
    return QodanaYamlItemProvider.provideAll(project, context)
  }

  @VisibleForTesting
  fun build(items: List<QodanaYamlItem>): String {
    return items
             .sortedBy { it.weight }
             .distinctBy { it.id }
             .joinToString("\n") { it.content } + "\n"
  }
}