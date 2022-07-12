// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.library.JSSyntheticLibraryProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER

class NuxtDotFolderSyntheticLibraryProvider : AdditionalLibraryRootsProvider(), JSSyntheticLibraryProvider {

  override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
    return PackageJsonFileManager.getInstance(project)
      .validPackageJsonFiles
      .mapNotNull {
        val dotNuxt = it.parent?.findChild(NUXT_OUTPUT_FOLDER) ?: return@mapNotNull null
        SyntheticLibrary.newImmutableLibrary("NuxtDotFolder::" + dotNuxt.url, listOf(dotNuxt), emptyList(), emptySet())
        { isDir, filename, _, _, _ ->
          if (isDir)
            filename != NUXT_OUTPUT_FOLDER
          else
            !TypeScriptUtil.isDefinitionFile(filename) &&
            //Using JsonUtil.isJsonFile here can produce StackOverflowError
            !StringUtil.endsWith(filename, ".json")
        }
      }
  }
}
