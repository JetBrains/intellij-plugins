// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.library.JSSyntheticLibraryProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER

class NuxtDotFolderSyntheticLibraryProvider : AdditionalLibraryRootsProvider(), JSSyntheticLibraryProvider {

  override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
    return PackageJsonFileManager.getInstance(project)
      .validPackageJsonFiles
      .mapNotNull { it ->
        val dotNuxt = it.parent?.findChild(NUXT_OUTPUT_FOLDER) ?: return@mapNotNull null
        SyntheticLibrary.newImmutableLibrary(listOf(dotNuxt), emptySet()) {
          if (it.isDirectory)
            it != dotNuxt
          else
            !TypeScriptUtil.isDefinitionFile(it) &&
            !isJsonFile(it)
        }
      }
  }

  /**
   * Using JsonUtil.isJsonFile here can produce StackOverflowError
   */
  private fun isJsonFile(file: VirtualFile): Boolean {
    return StringUtil.endsWith(file.nameSequence, ".json")
  }
}
