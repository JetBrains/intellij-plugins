// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.terraform.template.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.terraform.template.TftplFileType
import org.intellij.terraform.hil.psi.template.TftplLanguage

internal class TftplFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, TftplLanguage) {
  override fun getFileType(): FileType {
    return TftplFileType
  }

  override fun toString(): String {
    return "TftplFile"
  }
}