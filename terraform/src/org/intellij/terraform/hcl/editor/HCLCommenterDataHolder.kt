// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.editor

import com.intellij.codeInsight.generation.CommenterDataHolder
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile

class HCLCommenterDataHolder(file: PsiFile) : CommenterDataHolder() {
  init {
    putUserData(PSI_FILE_KEY, file)
  }

  val psiFile: PsiFile?
    get() = getUserData(PSI_FILE_KEY)

  companion object {
    private val PSI_FILE_KEY = Key.create<PsiFile>("HCL_COMMENTER_PSI_FILE")
  }
}