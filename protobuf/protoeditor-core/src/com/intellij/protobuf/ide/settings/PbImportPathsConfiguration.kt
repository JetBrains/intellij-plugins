@file:JvmName("PbImportPathsConfiguration")

package com.intellij.protobuf.ide.settings

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.protobuf.ide.PbCompositeModificationTracker
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry
import com.intellij.protobuf.lang.PbFileType
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.Plow
import com.intellij.util.asSafely
import java.util.stream.Stream
import kotlin.streams.asStream

fun computeDeterministicImportPathsStream(project: Project, pbSettings: PbProjectSettings): Stream<ImportPathEntry> {
  return computeDeterministicImportPaths(project, pbSettings).asStream()
}

fun computeDeterministicImportPaths(project: Project, pbSettings: PbProjectSettings): Sequence<ImportPathEntry> {
  return sequence {
    with(pbSettings) {
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
    ProjectRootManager.getInstance(project).let {
      it.contentRoots.map { ImportPathEntry(it.url, "") } +
      it.contentSourceRoots.map { ImportPathEntry(it.url, "") }
    }
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
        PbCompositeModificationTracker.getInstance(project)
      )
    }
}

internal fun getDescriptorPathSuggestions(project: Project): Collection<String> {
  return ProjectSettingsConfigurator.EP_NAME.getExtensions(project)
           .flatMap { configurator -> configurator.getDescriptorPathSuggestions(project) }
           .toSet() + BUNDLED_DESCRIPTOR
}

internal const val BUNDLED_DESCRIPTOR = "google/protobuf/descriptor.proto"

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

fun getOrComputeImportPathsForAllImportStatements(project: Project): List<String> {
  if (!PbProjectSettings.getInstance(project).isIndexBasedResolveEnabled) return emptyList()
  return CachedValuesManager.getManager(project)
    .getCachedValue(project) {
      val allDiscoveredImportPaths = computeImportPathsForAllImportStatements(project)
      CachedValueProvider.Result(
        allDiscoveredImportPaths,
        PbCompositeModificationTracker.getInstance(project),
        ProjectRootManager.getInstance(project),
      )
    }
}

internal fun computeImportPathsForAllImportStatements(project: Project): List<String> {
  val allProtoFiles = runReadAction { FileTypeIndex.getFiles(PbFileType.INSTANCE, GlobalSearchScope.allScope(project)) }
  val psiManager = PsiManager.getInstance(project)

  val importStatements = allProtoFiles.flatMap { virtualFile ->
    runReadAction {
      psiManager.findFile(virtualFile)?.asSafely<PbFile>()
        ?.importStatements
        ?.flatMap { importStatement -> importStatement.importName?.stringValue?.stringParts.orEmpty() }
        ?.mapNotNull { singleImport -> singleImport.text?.let(StringUtil::unquoteString) }
        .orEmpty()
    }
  }
    .filter(String::isNotBlank)
    .toSet()

  val allProtoFileUrls = runReadAction { allProtoFiles.map(VirtualFile::getUrl) }

  return allProtoFileUrls.asSequence()
    .flatMap { fileUrl ->
      importStatements.mapNotNull { importStatement ->
        fileUrl.substringWithoutSuffixOrNull(importStatement)
      }
    }.distinct()
    .toList()
}

private fun String.substringWithoutSuffixOrNull(suffix: String): String? {
  return when {
    suffix.isEmpty() -> null
    !this.endsWith(suffix) -> null
    else -> this.removeSuffix(suffix)
  }
}