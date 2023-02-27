// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.intellij.terraform.hil.HILFileType
import org.intellij.terraform.hil.HILLanguage

class ILPsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, HILLanguage) {
  override fun getFileType(): FileType {
    return HILFileType
  }

  override fun toString(): String {
    val virtualFile = virtualFile
    return "HILFile: " + (virtualFile?.name ?: "<unknown>")
  }
}
