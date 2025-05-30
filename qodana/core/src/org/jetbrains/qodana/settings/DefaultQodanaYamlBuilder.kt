package org.jetbrains.qodana.settings

import com.intellij.openapi.project.Project
import org.jetbrains.annotations.VisibleForTesting

class DefaultQodanaYamlBuilder(private val project: Project) {

  suspend fun build(githubPromo: Boolean = false): String {
    return build(getYamlItems(githubPromo))
  }

  private suspend fun getYamlItems(githubPromo: Boolean): List<QodanaYamlItem> {
    return QodanaYamlItemProvider.provideAll(project, DefaultQodanaItemContext(githubPromo))
  }

  @VisibleForTesting
  fun build(items: List<QodanaYamlItem>): String {
    return items
             .sortedBy { it.weight }
             .distinctBy { it.id }
             .joinToString("\n") { it.content } + "\n"
  }
}