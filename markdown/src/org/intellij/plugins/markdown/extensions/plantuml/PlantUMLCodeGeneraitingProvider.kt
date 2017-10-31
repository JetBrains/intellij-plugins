package org.intellij.plugins.markdown.extensions.plantuml

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.intellij.plugins.markdown.extensions.MarkdownCodeFencePluginGeneratingProvider
import org.intellij.plugins.markdown.extensions.MarkdownCodeFencePluginGeneratingProvider.Companion.markdownCachePath
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCache.MARKDOWN_FILE_PATH_KEY
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCacheProvider
import org.intellij.plugins.markdown.ui.preview.MarkdownUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal class PlantUMLPluginGeneratingProvider(private var pluginCache: MarkdownCodeFencePluginCacheProvider?) : MarkdownCodeFencePluginGeneratingProvider {
  constructor() : this(null)

  override fun getCacheRootPath(): String = "$markdownCachePath${File.separator}plantUML"

  override fun generateHtml(text: String): String {
    val newDiagramPath = File("${getCacheRootPath()}${File.separator}" +
                              "${MarkdownUtil.md5(pluginCache?.file?.path, MARKDOWN_FILE_PATH_KEY)}${File.separator}" +
                              "${MarkdownUtil.md5(text, "plantUML-diagram")}.png").absolutePath

    pluginCache?.addAliveCachedFile(LocalFileSystem.getInstance().refreshAndFindFileByPath(cachedDiagram(newDiagramPath, text))!!)

    return "<img src=\"file:${cachedDiagram(newDiagramPath, text)}\"></img>"
  }

  private fun cachedDiagram(newDiagramPath: String, text: String) =
    if (FileUtil.exists(newDiagramPath)) newDiagramPath else generateDiagram(text, newDiagramPath)

  @Throws(IOException::class)
  private fun generateDiagram(text: CharSequence, diagramPath: String): String {
    var innerText: String = text.toString().trim()
    if (!innerText.startsWith("@startuml")) innerText = "@startuml\n" + innerText
    if (!innerText.endsWith("@enduml")) innerText += "\n@enduml"

    FileUtil.createParentDirs(File(diagramPath))
    storeDiagram(innerText, diagramPath)

    return diagramPath
  }

  override fun isApplicable(languageString: String?): Boolean {
    return languageString == "puml" || languageString == "plantuml"
  }

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