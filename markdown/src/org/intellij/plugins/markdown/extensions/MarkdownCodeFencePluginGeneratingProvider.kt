package org.intellij.plugins.markdown.extensions

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

interface MarkdownCodeFencePluginGeneratingProvider {
  /**
   * Check if plugin applicable for code fence language string
   */
  fun isApplicable(language: String?): Boolean

  /**
   * Consumes code fence content
   */
  fun generateHtml(text: String): String

  /**
   * Code fence plugin name; used for caching
   */
  fun getCacheRootPath(): String

  companion object {
    val EP_NAME = ExtensionPointName.create<MarkdownCodeFencePluginGeneratingProvider>("org.intellij.markdown.codeFencePluginGeneratingProvider")
    val markdownCachePath = "${PathManager.getSystemPath()}${File.separator}markdown"
  }
}