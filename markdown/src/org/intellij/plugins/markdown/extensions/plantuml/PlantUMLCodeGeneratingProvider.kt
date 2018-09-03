package org.intellij.plugins.markdown.extensions.plantuml

import com.intellij.openapi.util.io.FileUtil
import org.intellij.plugins.markdown.extensions.MarkdownCodeFenceCacheableProvider
import org.intellij.plugins.markdown.settings.MarkdownSettingsConfigurable
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCache.MARKDOWN_FILE_PATH_KEY
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCacheCollector
import org.intellij.plugins.markdown.ui.preview.MarkdownUtil
import java.io.File
import java.io.IOException
import java.net.URLClassLoader

internal class PlantUMLProvider(private var cacheCollector: MarkdownCodeFencePluginCacheCollector?) : MarkdownCodeFenceCacheableProvider {
  // this empty constructor is needed for the component initialization
  constructor() : this(null)

  override fun generateHtml(text: String): String {
    val newDiagramFile = File("${getCacheRootPath()}${File.separator}" +
                              "${MarkdownUtil.md5(cacheCollector?.file?.path, MARKDOWN_FILE_PATH_KEY)}${File.separator}" +
                              "${MarkdownUtil.md5(text, "plantUML-diagram")}.png")

    cacheDiagram(newDiagramFile.absolutePath, text)
    cacheCollector?.addAliveCachedFile(newDiagramFile)

    return "<img src=\"${newDiagramFile.toURI()}\"/>"
  }

  private fun cacheDiagram(newDiagramPath: String, text: String) {
    if (!FileUtil.exists(newDiagramPath)) generateDiagram(text, newDiagramPath)
  }

  @Throws(IOException::class)
  private fun generateDiagram(text: CharSequence, diagramPath: String) {
    var innerText: String = text.toString().trim()
    if (!innerText.startsWith("@startuml")) innerText = "@startuml\n$innerText"
    if (!innerText.endsWith("@enduml")) innerText += "\n@enduml"

    FileUtil.createParentDirs(File(diagramPath))
    storeDiagram(innerText, diagramPath)
  }

  override fun isApplicable(language: String): Boolean = (language == "puml" || language == "plantuml")
                                                         && MarkdownSettingsConfigurable.isPlantUMLAvailable()

  companion object {
    private val sourceStringReader by lazyOf(Class.forName("net.sourceforge.plantuml.SourceStringReader", false, URLClassLoader(
      arrayOf(MarkdownSettingsConfigurable.getDownloadedJarPath()?.toURI()?.toURL()), this::class.java.classLoader)))

    private val generateImageMethod by lazyOf(sourceStringReader.getDeclaredMethod("generateImage", Class.forName("java.io.File")))


    @Throws(IOException::class)
    private fun storeDiagram(source: String, fileName: String) {
      generateImageMethod.invoke(sourceStringReader.getConstructor(String::class.java).newInstance(source), File(fileName))
    }
  }
}