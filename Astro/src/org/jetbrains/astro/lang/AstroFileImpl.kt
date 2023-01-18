// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class AstroFileImpl(provider: FileViewProvider)
  : PsiFileBase(provider, AstroLanguage.INSTANCE) {
  override fun getFileType(): FileType =
    AstroFileType.INSTANCE

  override fun toString(): String {
    return "AstroFile:$name"
  }
}