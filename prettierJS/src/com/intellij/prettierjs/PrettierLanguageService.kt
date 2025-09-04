// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.project.BaseProjectDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.CompletableFuture

interface PrettierLanguageService {
  fun format(
    filePath: String,
    ignoreFilePath: String?,
    text: String,
    prettierPackage: NodePackage,
    range: TextRange?,
  ): CompletableFuture<FormatResult?>

  fun resolveConfig(
    filePath: String,
    prettierPackage: NodePackage,
  ): CompletableFuture<ResolveConfigResult?>

  companion object {
    @JvmStatic
    fun getInstance(project: Project, contextFile: VirtualFile, prettierPackage: NodePackage): PrettierLanguageService {
      val packageJson = PackageJsonUtil.findUpPackageJson(contextFile)
      var workingDirectory = packageJson?.getParent()
      if (workingDirectory == null) {
        workingDirectory = BaseProjectDirectories.getInstance(project).getBaseDirectoryFor(contextFile)
      }
      if (workingDirectory == null) {
        workingDirectory = contextFile.getParent()
      }
      return PrettierLanguageServiceManager.getInstance(project)
        .useService<PrettierLanguageService, Throwable>(workingDirectory, NodePackageRef.create(prettierPackage)) { it }
    }
  }


  class FormatResult private constructor(
    @JvmField val result: String?,
    @JvmField val cursorOffset: Int,
    @JvmField val error: String?,
    @JvmField val ignored: Boolean,
    @JvmField val unsupported: Boolean,
  ) {
    companion object {
      val IGNORED: FormatResult = FormatResult(null, -1, null, true, false)

      @JvmField
      val UNSUPPORTED: FormatResult = FormatResult(null, -1, null, false, true)

      @JvmStatic
      fun error(error: String): FormatResult = FormatResult(null, -1, error, false, false)

      @JvmStatic
      fun formatted(result: String, cursorOffset: Int): FormatResult = FormatResult(result, cursorOffset, null, false, false)
    }
  }

  class ResolveConfigResult private constructor(
    @JvmField val config: PrettierConfig?,
    @JvmField val error: String?,
  ) {
    companion object {
      @JvmStatic
      fun error(error: String): ResolveConfigResult = ResolveConfigResult(null, error)

      @JvmStatic
      fun config(config: PrettierConfig): ResolveConfigResult = ResolveConfigResult(config, null)
    }
  }
}
