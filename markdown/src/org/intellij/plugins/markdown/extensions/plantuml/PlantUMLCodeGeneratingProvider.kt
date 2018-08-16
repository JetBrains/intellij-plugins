package org.intellij.plugins.markdown.extensions.plantuml

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.containers.ContainerUtil
import org.intellij.plugins.markdown.extensions.MarkdownCodeFenceCacheableProvider
import org.intellij.plugins.markdown.settings.MarkdownSettingsConfigurable
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCache.MARKDOWN_FILE_PATH_KEY
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCacheCollector
import org.intellij.plugins.markdown.ui.preview.MarkdownUtil
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

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
    private val LOG = Logger.getInstance(PlantUMLCodeFenceLanguageProvider::class.java)
    @Throws(IOException::class)
    private fun storeDiagram(source: String, fileName: String) {
      val commandLine = GeneralCommandLine(getShellCommand() ?: run {
        LOG.warn("Cannot find shell to generate the plantUML diagram.")
        return
      })

      commandLine.addParameter("echo \"$source\" " +
                               "| java -Djava.awt.headless=true -jar ${MarkdownSettingsConfigurable.getDownloadedJarPath().absolutePath} -pipe " +
                               "> $fileName")

      commandLine.createProcess().waitFor(5, TimeUnit.SECONDS)
    }

    private fun getShellCommand(): List<String>? {
      if (SystemInfoRt.isWindows) return ContainerUtil.immutableList(ExecUtil.windowsShellName, "/c")

      val shell = System.getenv("SHELL")
      if (shell == null || !File(shell).canExecute()) {
        return null
      }

      val commands = ContainerUtil.newArrayList(shell)
      if (!shell.endsWith("/tcsh") && !shell.endsWith("/csh")) {
        commands.add("--login")
      }
      commands.add("-c")
      return commands
    }
  }
}