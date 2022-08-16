// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.library.JSSyntheticLibraryProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.util.text.StringUtil

import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import org.jetbrains.vuejs.libraries.nuxt.NUXT_DIST_SUBFOLDER
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER
import org.jetbrains.vuejs.libraries.nuxt.index.NuxtIndexExcludePolicy

/**
 * @see NuxtIndexExcludePolicy
 */
class NuxtDotFolderSyntheticLibraryProvider : AdditionalLibraryRootsProvider(), JSSyntheticLibraryProvider {
  override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
    return PackageJsonFileManager.getInstance(project)
      .validPackageJsonFiles
      .mapNotNull {
        val dotNuxt = it.parent?.findChild(NUXT_OUTPUT_FOLDER) ?: return@mapNotNull null
        val includedFiles = mutableListOf<VirtualFile>()

        VfsUtilCore.visitChildrenRecursively(dotNuxt, object : VirtualFileVisitor<Any>() {
          override fun visitFileEx(file: VirtualFile): Result {
            if (file.isDirectory && file.name == NUXT_DIST_SUBFOLDER) {
              return SKIP_CHILDREN
            }

            if (TypeScriptUtil.isDefinitionFile(file.name) ||
                //Using JsonUtil.isJsonFile here can produce StackOverflowError
                StringUtil.endsWith(file.name, ".json")) {
              includedFiles.add(file)
            }

            return CONTINUE
          }
        })

        SyntheticLibrary.newImmutableLibrary(includedFiles)
      }
  }
}
