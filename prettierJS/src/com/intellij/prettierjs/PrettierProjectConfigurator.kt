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

class PrettierProjectConfigurator: DirectoryProjectConfigurator.AsyncDirectoryProjectConfigurator() {
  override suspend fun configure(project: Project, baseDir: VirtualFile, moduleRef: Ref<Module>, isProjectCreatedWithWizard: Boolean) {
    readAndWriteAction {
      val config = PrettierConfiguration.getInstance(project)

      // checking default configuration mode for preventing overriding after unexpected IDE closing
      if (config.isDefaultConfigurationMode && shouldAutoconfigurePrettierForDirectory(baseDir)) {
        writeAction {
          config.state.configurationMode = ConfigurationMode.AUTOMATIC
        }
      }
      else value(Unit)
    }
  }

  private fun shouldAutoconfigurePrettierForDirectory(dir: VirtualFile): Boolean {
    if (!dir.isDirectory) return false
    val packageJson = dir.findChild(PackageJsonUtil.FILE_NAME) ?: return false
    val packageJsonData = PackageJsonData.getOrCreate(packageJson)
    if (!packageJsonData.containsOneOfDependencyOfAnyType(PrettierUtil.PACKAGE_NAME)) return false
    return packageJsonData.topLevelProperties.contains(PrettierUtil.CONFIG_SECTION_NAME)
           || PrettierUtil.findSingleConfigInDirectory(dir) != null
  }
}
