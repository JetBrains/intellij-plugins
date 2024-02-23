// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.intellij.terraform.hil.psi.TerraformTemplateLanguage
import org.intellij.terraform.template.TerraformTemplateFileType

class TftplFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, TerraformTemplateLanguage) {
  override fun getFileType(): FileType {
    return TerraformTemplateFileType
  }

  override fun toString(): String {
    return "TftplFile"
  }
}