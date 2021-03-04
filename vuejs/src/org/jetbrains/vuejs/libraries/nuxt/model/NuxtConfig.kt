// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.model

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

interface NuxtConfig {

  val file: PsiFile?
    get() = null

  val sourceDir: VirtualFile?
    get() = null

  val components: List<ComponentsDirectoryConfig>
    get() = emptyList()

  interface ComponentsDirectoryConfig {
    val path: String
    val prefix: String
    val global: Boolean
    val extensions: Set<String>
    val level: Int
  }

}