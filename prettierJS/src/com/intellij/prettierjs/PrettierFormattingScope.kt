// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.linter.GlobPatternUtil
import com.intellij.openapi.project.BaseProjectDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile

internal fun isPrettierFormattingAllowedFor(
  project: Project,
  file: VirtualFile,
  checkIsInContent: Boolean = true,
): Boolean {
  val config = PrettierConfiguration.getInstance(project)

  if (checkIsInContent && !ProjectFileIndex.getInstance(project).isInContent(file)) {
    return false
  }

  if (!GlobPatternUtil.isFileMatchingGlobPattern(project, config.filesPattern, file)) {
    return false
  }

  if (!config.formatFilesOutsideDependencyScope) {
    return findPrettierScopeRootFor(project, file) != null
  }

  return true
}

internal fun findPrettierScopeRootFor(project: Project, file: VirtualFile): VirtualFile? {
  val startDir = if (file.isDirectory()) file else file.getParent()
  if (startDir == null) return null

  val baseDir = BaseProjectDirectories.getInstance(project).getBaseDirectoryFor(file) ?: return null

  var currentDir: VirtualFile? = startDir
  while (currentDir != null && currentDir.isValid() && currentDir.isDirectory()) {
    val packageJson = PackageJsonUtil.findChildPackageJsonFile(currentDir)
    if (packageJson != null) {
      val data = PackageJsonData.getOrCreate(packageJson)
      if (data.isDependencyOfAnyType(PrettierUtil.PACKAGE_NAME)) {
        return currentDir
      }
    }

    val prettierConfigFile = PrettierUtil.findChildConfigFile(currentDir)
    if (prettierConfigFile != null) {
      return currentDir
    }

    if (baseDir == currentDir) {
      break
    }

    currentDir = currentDir.getParent()
  }

  return null
}
