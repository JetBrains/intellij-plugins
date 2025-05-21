package org.jetbrains.qodana.settings

import org.jetbrains.annotations.VisibleForTesting

abstract class QodanaYamlBuilder {

  suspend fun build(): String {
     return build(getYamlItems())
   }

   protected abstract suspend fun getYamlItems(): List<QodanaYamlItem>

  @VisibleForTesting
  fun build(items: List<QodanaYamlItem>): String {
    return items
             .sortedBy { it.weight }
             .distinctBy { it.id }
             .joinToString("\n") { it.content } + "\n"
  }
}
