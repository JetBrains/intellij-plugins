package org.intellij.plugins.markdown.extensions

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

interface MarkdownCodeFencePluginGeneratingProvider {
  /**
   * Check if plugin applicable for code fence language string
   */
  fun isApplicable(languageString: String?): Boolean

  /**
   * Consumes code fence content
   */
  fun generateHtml(text: String): String

  /**
   * Code fence plugin name; used for caching
   */
  fun getCacheRoot(): VirtualFile

  companion object {
    val markdownCachePath = "${PathManager.getSystemPath()}${File.separator}markdown"
  }
}