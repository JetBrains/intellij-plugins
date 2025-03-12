// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.intellij.terraform.hil.psi.template.TftplLanguage
import org.intellij.terraform.template.TftplFileType

class TftplFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, TftplLanguage) {
  override fun getFileType(): FileType {
    return TftplFileType
  }

  override fun toString(): String {
    return "TftplFile"
  }
}