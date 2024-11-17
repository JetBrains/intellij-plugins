package org.jetbrains.qodana.settings

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class QodanaYamlItem(
  val id: String,
  val weight: Int,
  val content: String
)

interface QodanaYamlItemProvider {
  companion object {
    private val EP = ExtensionPointName<QodanaYamlItemProvider>("org.intellij.qodana.defaultQodanaYamlItemProvider")

    suspend fun provideAll(project: Project): List<QodanaYamlItem> {
      return coroutineScope {
        EP.extensionList.map {
          async { it.provide(project) }
        }.awaitAll().filterNotNull()
      }
    }
  }

  suspend fun provide(project: Project): QodanaYamlItem?
}