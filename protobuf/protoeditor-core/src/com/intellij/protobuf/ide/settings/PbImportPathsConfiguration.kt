@file:JvmName("PbImportPathsConfiguration")

package com.intellij.protobuf.ide.settings

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry
import com.intellij.protobuf.lang.PbFileType
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.Plow
import java.util.stream.Stream
import kotlin.streams.asStream

fun computeImportPathsStream(project: Project): Stream<ImportPathEntry> {
  return computeImportPaths(project).asStream()
}

fun computeImportPaths(project: Project): Sequence<ImportPathEntry> {
  return sequence {
    with(PbProjectSettings.getInstance(project)) {
      yieldAll(configuredImportPaths(project))
      if (isIncludeProtoDirectories) yieldAll(standardProtoDirectories(project))
      if (isIncludeContentRoots) yieldAll(projectContentRoots(project))
      if (isThirdPartyConfigurationEnabled) yieldAll(thirdPartyImportPaths(project))
      getBuiltInIncludeEntry()?.let { yield(it) }
    }
  }
}

internal fun projectContentRoots(project: Project): List<ImportPathEntry> {
  return runReadAction {
    ProjectRootManager.getInstance(project).contentRootUrls.map { ImportPathEntry(it, "") }
  }
}

private fun configuredImportPaths(project: Project): List<ImportPathEntry> {
  return PbProjectSettings.getInstance(project).importPathEntries
}

internal fun thirdPartyImportPaths(project: Project): List<ImportPathEntry> {
  // avoid working with a service class in the future - requires public API change
  val emptySettings = PbProjectSettings(project)
  return ProjectSettingsConfigurator.EP_NAME.getExtensions(project)
    .firstNotNullOfOrNull { configurator ->
      runReadAction { configurator.configure(project, emptySettings) }
    }
    ?.importPathEntries.orEmpty()
}

internal fun standardProtoDirectories(project: Project): List<ImportPathEntry> {
  return CachedValuesManager.getManager(project)
    .getCachedValue(project) {
      val protoDirectories = runReadAction {
        Plow.of { processor ->
          FilenameIndex.processFilesByNames(setOf("proto", "protobuf"), true, GlobalSearchScope.projectScope(project), null, processor)
        }.filter { file -> file.isDirectory }
          .map { directory -> ImportPathEntry(directory.url, "") }
          .toList()
      }

      CachedValueProvider.Result(
        protoDirectories,
        ProjectRootManager.getInstance(project),
        PbProjectSettings.getInstance(project).state
      )
    }
}

internal fun getDescriptorPathSuggestions(project: Project): Collection<String> {
  return ProjectSettingsConfigurator.EP_NAME.getExtensions(project)
    .flatMap { configurator -> configurator.getDescriptorPathSuggestions(project) }
    .toSet()
}

internal fun getBuiltInIncludeEntry(): ImportPathEntry? {
  val includedDescriptorsDirectoryUrl = PbProjectSettings::class.java.classLoader.getResource("include") ?: return null
  val descriptorsDirectory = VfsUtil.findFileByURL(includedDescriptorsDirectoryUrl)
  return if (descriptorsDirectory == null || !descriptorsDirectory.isDirectory) {
    null
  }
  else ImportPathEntry(descriptorsDirectory.url, "")
}

internal fun findFileByImportPath(searchedFileName: String,
                                  searchScope: GlobalSearchScope,
                                  path: String): VirtualFile? {
  return Plow.of { processor ->
    FileTypeIndex.processFiles(PbFileType.INSTANCE, processor, searchScope)
  }.find { file ->
    file.nameWithoutExtension == searchedFileName && file.url.endsWith(path)
  }
}

internal fun findAllDirectoriesWithCorrespondingImportPaths(project: Project): List<ImportPathEntry> {
  // todo for request execution
  return Plow.of { processor ->
    FileTypeIndex.processFiles(PbFileType.INSTANCE, processor, GlobalSearchScope.allScope(project))
  }.map { file -> file.parent }
    .map { directory -> ImportPathEntry(directory.url, "") }
    .toList()
}