// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.json.JsonFileType
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import org.jetbrains.vuejs.libraries.nuxt.NUXT_DIST_SUBFOLDER

internal sealed interface NuxtFolderLibrary {
  val nuxtFolder: VirtualFile
  val libraryFiles: List<VirtualFile>
}

internal class NuxtFolderNotReadyLibrary(override val nuxtFolder: VirtualFile) : NuxtFolderLibrary {
  override val libraryFiles: List<VirtualFile>
    get() = emptyList()
}

internal class NuxtFolderReadyLibrary private constructor(
  override val nuxtFolder: VirtualFile,
  override val libraryFiles: List<VirtualFile>,
): NuxtFolderLibrary {

  companion object {
    suspend fun create(nuxtFolder: VirtualFile): NuxtFolderReadyLibrary {
      val includedFiles = mutableSetOf<VirtualFile>()
      val job = currentCoroutineContext().job
      VfsUtilCore.visitChildrenRecursively(nuxtFolder, object : VirtualFileVisitor<Any>() {
        override fun visitFileEx(file: VirtualFile): Result {
          job.ensureActive()
          if (file.isDirectory && file.name == NUXT_DIST_SUBFOLDER) {
            return SKIP_CHILDREN
          }
          if (TypeScriptUtil.isDefinitionFile(file.name) || file.fileType == JsonFileType.INSTANCE) {
            includedFiles.add(file)
          }
          return CONTINUE
        }
      })
      return NuxtFolderReadyLibrary(nuxtFolder, includedFiles.toList())
    }
  }
}
