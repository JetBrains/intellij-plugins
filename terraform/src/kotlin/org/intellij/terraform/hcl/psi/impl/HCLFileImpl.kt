// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.intellij.terraform.hcl.HILCompatibleLanguage
import org.intellij.terraform.hcl.psi.HCLFile

class HCLFileImpl(fileViewProvider: FileViewProvider, language: Language) : PsiFileBase(fileViewProvider, language), HCLFile {
  override fun isInterpolationsAllowed(): Boolean {
    return language is HILCompatibleLanguage
  }

  override fun getFileType(): FileType {
    return viewProvider.virtualFile.fileType
  }

  override fun toString(): String {
    return "HCLFile: " + (virtualFile?.name ?: "<unknown>")
  }
}
