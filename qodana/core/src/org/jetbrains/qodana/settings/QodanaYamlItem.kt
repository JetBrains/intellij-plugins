package org.jetbrains.qodana.settings

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Used to determine which QodanaYamlItems should be provided in [QodanaYamlItemProvider.provideAll]
 * Should be used in [QodanaYamlItemProvider.provide]
 */
sealed class QodanaYamlItemContext
class QodanaYamlItem(
  val id: String,
  val weight: Int,
  val content: String
)

interface QodanaYamlItemProvider {
  companion object {
    private val EP = ExtensionPointName<QodanaYamlItemProvider>("org.intellij.qodana.qodanaYamlItemProvider")

    suspend fun provideAll(project: Project, context: QodanaYamlItemContext): List<QodanaYamlItem> {
      return coroutineScope {
        EP.extensionList.map {
          async { it.provide(project, context) }
        }.awaitAll().filterNotNull()
      }
    }
  }

  suspend fun provide(project: Project, context: QodanaYamlItemContext): QodanaYamlItem?
}