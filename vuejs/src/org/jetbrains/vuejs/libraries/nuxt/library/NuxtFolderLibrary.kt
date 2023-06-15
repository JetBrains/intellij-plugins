// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.json.JsonFileType
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import org.jetbrains.vuejs.libraries.nuxt.NUXT_DIST_SUBFOLDER

class NuxtFolderLibrary(val nuxtFolder: VirtualFile) {
  val libraryFiles: List<VirtualFile> = getIncludedFiles(nuxtFolder)

  private fun getIncludedFiles(nuxtFolder: VirtualFile): List<VirtualFile> {
    val includedFiles = mutableSetOf<VirtualFile>()
    VfsUtilCore.visitChildrenRecursively(nuxtFolder, object : VirtualFileVisitor<Any>() {
      override fun visitFileEx(file: VirtualFile): Result {
        if (file.isDirectory && file.name == NUXT_DIST_SUBFOLDER) {
          return SKIP_CHILDREN
        }
        if (TypeScriptUtil.isDefinitionFile(file.name) || file.fileType == JsonFileType.INSTANCE) {
          includedFiles.add(file)
        }
        return CONTINUE
      }
    })
    return includedFiles.toList()
  }
}