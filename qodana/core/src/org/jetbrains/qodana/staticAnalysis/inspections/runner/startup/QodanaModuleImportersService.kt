package org.jetbrains.qodana.staticAnalysis.inspections.runner.startup

import com.intellij.openapi.components.Service
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.isFile
import com.intellij.platform.PlatformProjectOpenProcessor
import com.intellij.projectImport.ProjectOpenProcessor
import com.intellij.util.application
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.qodanaAnalysisConfigForConfiguration
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.nio.file.Path
import kotlin.reflect.KClass

@Service
class QodanaManualModuleImportersService() : QodanaModuleImportersService() {
  override val importers: Map<out ProjectOpenProcessor, List<VirtualFile>> by lazy {
    val config = loadQodanaConfig()
    val resolvedPaths = config.rootJavaProjects.map { if (it.isAbsolute) it else config.projectPath.resolve(it) }
    searchImportersFor(resolvedPaths)
  }
}

@Service
class QodanaAutomaticModuleImportersService() : QodanaModuleImportersService() {
  override val importers: Map<out ProjectOpenProcessor, List<VirtualFile>> by lazy {
    autoDetectImporters(loadQodanaConfig().projectPath, 5)
  }
}

private fun loadQodanaConfig(): QodanaConfig =
  application.qodanaAnalysisConfigForConfiguration ?: throw QodanaException("Qodana configuration not found")


abstract class QodanaModuleImportersService() {
  abstract val importers: Map<out ProjectOpenProcessor, List<VirtualFile>>
  fun <T : ProjectOpenProcessor> getFilesForImporter(clazz: KClass<out T>): List<VirtualFile> {
    return importers.entries.filter { it.key::class == clazz }.map { it.value }.firstOrNull() ?: emptyList()
  }
}

private fun autoDetectImporters(projectPath: Path, scanDepthLimit: Int = 1): Map<ProjectOpenProcessor, List<VirtualFile>> {
  val projectVirtualFile = VfsUtil.findFile(projectPath.normalize(), true) ?: return emptyMap()
  if (projectVirtualFile.isFile) {
    return findProjectOpenProcessors(projectVirtualFile).associateWith { listOf(projectVirtualFile) }
  }

  val providersAndFiles = mutableMapOf<ProjectOpenProcessor, MutableList<VirtualFile>>() //look for project files among children first and go recursively into directories only if not found
  VfsUtil.visitChildrenRecursively(projectVirtualFile, object : VirtualFileVisitor<Void>(NO_FOLLOW_SYMLINKS, limit(scanDepthLimit)) {
    override fun visitFileEx(file: VirtualFile): Result {
      if (file.isDirectory) {
        if (file.name == ".idea" || FileTypeRegistry.getInstance().isFileIgnored(file)) {
          return SKIP_CHILDREN
        }
        val childrenFiles = file.getChildren().filter { it.isFile }
        var projectFileCandidatesFound = false
        for (childFile in childrenFiles) {
          findProjectOpenProcessors(childFile).forEach {
            providersAndFiles.computeIfAbsent(it) { mutableListOf() }.add(childFile)
            projectFileCandidatesFound = true
          }
        }
        if (projectFileCandidatesFound) {
          return SKIP_CHILDREN
        }
      }
      return CONTINUE
    }
  })
  return providersAndFiles
}

private fun searchImportersFor(rootJavaProjects: List<Path>): Map<ProjectOpenProcessor, List<VirtualFile>> {
  return rootJavaProjects.flatMap { autoDetectImporters(it).asSequence() }
    .groupBy({ it.key }, { it.value })
    .mapValues { (_, values) -> values.flatten() }
}

private fun findProjectOpenProcessors(virtualFile: VirtualFile): Set<ProjectOpenProcessor> {
  return ProjectOpenProcessor.EXTENSION_POINT_NAME.extensionList.filter {
    it !is PlatformProjectOpenProcessor && it.canOpenProject(virtualFile)
  }.toSet()
}
