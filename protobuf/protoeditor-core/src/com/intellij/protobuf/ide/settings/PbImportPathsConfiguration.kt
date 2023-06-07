package com.intellij.protobuf.ide.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.Plow
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.containers.addIfNotNull

@Service(Service.Level.PROJECT)
class PbImportPathsConfiguration(private val myProject: Project) {

  companion object {
    @JvmStatic
    fun getInstance(project: Project): PbImportPathsConfiguration = project.service<PbImportPathsConfiguration>()
  }

  @RequiresReadLock
  fun getOrComputeImportPaths(): List<ImportPathEntry> {
    return CachedValuesManager.getManager(myProject)
      .getCachedValue(myProject) {
        CachedValueProvider.Result(
          computeEffectiveImportPathsList(myProject),
          ProjectRootManager.getInstance(myProject),
          PbProjectSettings.getInstance(myProject).state
        )
      }
  }

  private fun computeEffectiveImportPathsList(project: Project): List<ImportPathEntry> {
    return buildList {
      with(PbProjectSettings.getInstance(project)) {
        addAll(configuredImportPaths())
        if (isIncludeProtoDirectories) addAll(standardProtoDirectories()) // todo state!! + cache on pb file type tracker
        if (isIncludeContentRoots) addAll(projectContentRoots())
        if (isThirdPartyConfigurationEnabled) addAll(thirdPartyImportPaths()) //todo state
        addIfNotNull(getBuiltInIncludeEntry())
      }
    }
  }

  fun projectContentRoots(): List<ImportPathEntry> {
    return ProjectRootManager.getInstance(myProject).contentRootUrls.map { ImportPathEntry(it, "") }
  }

  private fun configuredImportPaths(): List<ImportPathEntry> {
    return PbProjectSettings.getInstance(myProject).importPathEntries
  }

  fun thirdPartyImportPaths(): List<ImportPathEntry> {
    // avoid working with a service class in the future - requires public API change
    val emptySettings = PbProjectSettings(myProject)
    return ProjectSettingsConfigurator.EP_NAME.getExtensions(myProject)
      .firstNotNullOfOrNull { configurator ->
        configurator.configure(myProject, emptySettings)
      }
      ?.importPathEntries.orEmpty()
  }

  fun standardProtoDirectories(): List<ImportPathEntry> {
    return Plow.of { processor ->
      FilenameIndex.processFilesByNames(setOf("proto", "protobuf"), true, GlobalSearchScope.projectScope(myProject), null, processor)
    }.filter { file -> file.isDirectory }
      .map { directory -> ImportPathEntry(directory.url, "") }
      .toList()
  }

  fun getDescriptorPathSuggestions(): Collection<String> {
    return ProjectSettingsConfigurator.EP_NAME.getExtensions(myProject)
      .flatMap { configurator -> configurator.getDescriptorPathSuggestions(myProject) }
      .toSet()
  }

  fun getBuiltInIncludeEntry(): ImportPathEntry? {
    val includedDescriptorsDirectoryUrl = PbImportPathsConfiguration::class.java.classLoader.getResource("include") ?: return null
    val descriptorsDirectory = VfsUtil.findFileByURL(includedDescriptorsDirectoryUrl)
    return if (descriptorsDirectory == null || !descriptorsDirectory.isDirectory) {
      null
    }
    else ImportPathEntry(descriptorsDirectory.url, "")
  }
}