package org.intellij.plugins.markdown.extensions.plantuml

import com.intellij.openapi.util.io.FileUtil
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.intellij.plugins.markdown.extensions.MarkdownCodeFenceCacheableProvider
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCache.MARKDOWN_FILE_PATH_KEY
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCacheCollector
import org.intellij.plugins.markdown.ui.preview.MarkdownUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

  override fun isApplicable(language: String): Boolean = language == "puml" || language == "plantuml"

  companion object {
    @Throws(IOException::class)
    private fun storeDiagram(source: String, fileName: String) {
      val reader = SourceStringReader(source)
      val fos = FileOutputStream(fileName)

      reader.outputImage(fos, FileFormatOption(FileFormat.PNG))
      fos.close()
    }
  }
}