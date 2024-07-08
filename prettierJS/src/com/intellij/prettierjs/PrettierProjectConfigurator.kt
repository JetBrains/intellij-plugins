// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.application.readAndWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectConfigurator
import com.intellij.prettierjs.PrettierConfiguration.ConfigurationMode

class PrettierProjectConfigurator : DirectoryProjectConfigurator.AsyncDirectoryProjectConfigurator() {
  override suspend fun configure(project: Project, baseDir: VirtualFile, moduleRef: Ref<Module>, isProjectCreatedWithWizard: Boolean) {
    readAndWriteAction {
      val config = PrettierConfiguration.getInstance(project)

      // checking default configuration mode for preventing overriding after unexpected IDE closing
      if (config.isDefaultConfigurationMode) {
        val detectionInfo = getPrettierDetectionInfo(baseDir)

        if (detectionInfo.hasDependencyOnPrettier && !detectionInfo.hasPrettierConfig) {
          PrettierConfigurationCollector.logAutoEnableInNewProject(
            PrettierConfigurationCollector.EnabledStatus.UNCHANGED,
            PrettierConfigurationCollector.PackageDeclarationLocation.IN_PROJECT_ROOT_PACKAGE
          )
        }
        else if (shouldAutoconfigurePrettierForRootDirectory(detectionInfo)) {
          return@readAndWriteAction writeAction {
            config.state.configurationMode = ConfigurationMode.AUTOMATIC

            PrettierConfigurationCollector.logAutoEnableInNewProject(
              PrettierConfigurationCollector.EnabledStatus.AUTOMATIC,
              PrettierConfigurationCollector.PackageDeclarationLocation.IN_PROJECT_ROOT_PACKAGE,
              getPrettierConfigLocation(detectionInfo)
            )
          }
        }
      }
      value(Unit)
    }
  }

  private fun getRootPackageJsonData(dir: VirtualFile): PackageJsonData? {
    if (!dir.isDirectory) return null
    val packageJson = dir.findChild(PackageJsonUtil.FILE_NAME) ?: return null
    return PackageJsonData.getOrCreate(packageJson)
  }

  private fun getPrettierDetectionInfo(dir: VirtualFile): PrettierDetectionInfo {
    val packageJsonData = getRootPackageJsonData(dir) ?: return PrettierDetectionInfo()
    if (!packageJsonData.containsOneOfDependencyOfAnyType(PrettierUtil.PACKAGE_NAME)) {
      return PrettierDetectionInfo(true)
    }
    return PrettierDetectionInfo(true,
                                 true,
                                 packageJsonData.topLevelProperties.contains(PrettierUtil.CONFIG_SECTION_NAME),
                                 PrettierUtil.findSingleConfigInDirectory(dir) != null)
  }

  private fun shouldAutoconfigurePrettierForRootDirectory(detectionInfo: PrettierDetectionInfo): Boolean =
    detectionInfo.hasPackageJsonInProjectRoot && detectionInfo.hasDependencyOnPrettier && detectionInfo.hasPrettierConfig

  private fun getPrettierConfigLocation(detectionInfo: PrettierDetectionInfo): PrettierConfigurationCollector.ConfigLocation {
    if (detectionInfo.hasPrettierConfigInPackageJson) {
      return PrettierConfigurationCollector.ConfigLocation.PACKAGE_JSON
    }
    else if (detectionInfo.hasPrettierConfigFile) {
      return PrettierConfigurationCollector.ConfigLocation.CONFIG_FILE
    }
    return PrettierConfigurationCollector.ConfigLocation.NONE
  }
}

private data class PrettierDetectionInfo(
  val hasPackageJsonInProjectRoot: Boolean = false,
  val hasDependencyOnPrettier: Boolean = false,
  val hasPrettierConfigInPackageJson: Boolean = false,
  val hasPrettierConfigFile: Boolean = false,
) {
  val hasPrettierConfig: Boolean get() = hasPrettierConfigInPackageJson || hasPrettierConfigFile
}
