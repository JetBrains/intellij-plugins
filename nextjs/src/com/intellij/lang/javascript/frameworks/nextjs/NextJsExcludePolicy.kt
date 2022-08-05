package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy
import com.intellij.openapi.vfs.ex.temp.TempFileSystem

class NextJsExcludePolicy(private val project: Project) : DirectoryIndexExcludePolicy {

  override fun getExcludeUrlsForProject(): Array<String> {
    if (project.isDefault) return emptyArray()

    return PackageJsonFileManager.getInstance(project)
      .validPackageJsonFiles
      .asSequence()
      .mapNotNull { it.parent }
      .filter { it.fileSystem !is TempFileSystem }
      .map { "${it.url}/.next" }
      .toList()
      .toTypedArray()
  }
}