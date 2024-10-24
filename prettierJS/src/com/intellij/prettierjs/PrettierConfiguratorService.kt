// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.openapi.application.ReadConstraint
import com.intellij.openapi.application.ReadResult
import com.intellij.openapi.application.ReadResult.Companion.writeAction
import com.intellij.openapi.application.constrainedReadAndWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.prettierjs.PrettierConfiguration.ConfigurationMode
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

@Service(Service.Level.PROJECT)
class PrettierConfiguratorService(private val project: Project, @VisibleForTesting val cs: CoroutineScope) {

  fun configure(baseDir: VirtualFile) {
    cs.launch {
      constrainedReadAndWriteAction(ReadConstraint.inSmartMode(project)) {
        return@constrainedReadAndWriteAction detectAndSetUp(baseDir)
      }
    }
  }

  private fun detectAndSetUp(baseDir: VirtualFile): ReadResult<Unit> {
    val config = PrettierConfiguration.getInstance(project)

    // checking default configuration mode for preventing overriding after unexpected IDE closing
    if (!config.isDefaultConfigurationMode) return ReadResult.value(Unit)

    val (packageDeclarationLocation, configLocation) = getDetectionInfo(baseDir)

    if (packageDeclarationLocation != PrettierConfigurationCollector.PackageDeclarationLocation.NONE) {
      when (configLocation) {
        PrettierConfigurationCollector.ConfigLocation.NONE -> {
          PrettierConfigurationCollector.logAutoEnableInNewProject(
            PrettierConfigurationCollector.EnabledStatus.UNCHANGED,
            packageDeclarationLocation,
            configLocation,
          )
        }
        else -> {
          return writeAction {
            config.state.configurationMode = ConfigurationMode.AUTOMATIC

            PrettierConfigurationCollector.logAutoEnableInNewProject(
              PrettierConfigurationCollector.EnabledStatus.AUTOMATIC,
              packageDeclarationLocation,
              configLocation,
            )
          }
        }
      }
    }

    return ReadResult.value(Unit)
  }

  @RequiresBackgroundThread
  @RequiresReadLock
  private fun getDetectionInfo(baseDir: VirtualFile): DetectionInfo {
    val packageJsonFiles = PackageJsonFileManager.getInstance(project)
      .getValidPackageJsonFiles()
      .map { PackageJsonData.getOrCreate(it) }
      .filter { it.containsOneOfDependencyOfAnyType(PrettierUtil.PACKAGE_NAME) }

    val detectionMetrics = packageJsonFiles.fold(DetectionMetrics()) { acc, packageJson ->
      val directory = packageJson.packageJsonFile.parent
      acc.apply {
        if (directory == baseDir) {
          hasRootDependency = true
        }
        else {
          dependencySubdirectoryCount++
        }
        hasConfigInPackageJson = hasConfigInPackageJson || packageJson.hasConfigSection()
        hasConfigFile = hasConfigFile || PrettierUtil.findSingleConfigInDirectory(directory) != null
      }
    }

    val packageDeclarationLocation = determinePackageDeclarationLocation(detectionMetrics)
    val configLocation = determineConfigLocation(detectionMetrics)

    return DetectionInfo(packageDeclarationLocation, configLocation)
  }

  private fun determinePackageDeclarationLocation(metrics: DetectionMetrics): PrettierConfigurationCollector.PackageDeclarationLocation =
    when {
      metrics.hasRootDependency -> PrettierConfigurationCollector.PackageDeclarationLocation.IN_PROJECT_ROOT_PACKAGE
      metrics.dependencySubdirectoryCount > 1 -> PrettierConfigurationCollector.PackageDeclarationLocation.IN_MULTIPLE_SUBDIR_PACKAGES
      metrics.dependencySubdirectoryCount == 1 -> PrettierConfigurationCollector.PackageDeclarationLocation.IN_SUBDIR_PACKAGE
      else -> PrettierConfigurationCollector.PackageDeclarationLocation.NONE
    }

  private fun determineConfigLocation(metrics: DetectionMetrics): PrettierConfigurationCollector.ConfigLocation =
    when {
      metrics.hasConfigFile && metrics.hasConfigInPackageJson -> PrettierConfigurationCollector.ConfigLocation.MIXED
      metrics.hasConfigFile -> PrettierConfigurationCollector.ConfigLocation.CONFIG_FILE
      metrics.hasConfigInPackageJson -> PrettierConfigurationCollector.ConfigLocation.PACKAGE_JSON
      else -> PrettierConfigurationCollector.ConfigLocation.NONE
    }

  private data class DetectionInfo(
    val packageDeclarationLocation: PrettierConfigurationCollector.PackageDeclarationLocation,
    val configLocation: PrettierConfigurationCollector.ConfigLocation,
  )

  private data class DetectionMetrics(
    var hasRootDependency: Boolean = false,
    var dependencySubdirectoryCount: Int = 0,
    var hasConfigInPackageJson: Boolean = false,
    var hasConfigFile: Boolean = false,
  )

  private fun PackageJsonData.hasConfigSection(): Boolean =
    topLevelProperties.contains(PrettierUtil.CONFIG_SECTION_NAME)
}