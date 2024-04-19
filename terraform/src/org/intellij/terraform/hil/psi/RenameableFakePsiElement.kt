// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.FakePsiElement

/**
 * Based on com.intellij.psi.impl.RenameableFakePsiElement
 */
abstract class RenameableFakePsiElement protected constructor(private val myParent: PsiElement) : FakePsiElement() {

  override fun getParent(): PsiElement {
    return myParent
  }

  override fun getContainingFile(): PsiFile {
    return myParent.containingFile
  }

  abstract override fun getName(): String?

  override fun getLanguage(): Language {
    return containingFile.language
  }

  override fun getProject(): Project {
    return myParent.project
  }

  override fun getManager(): PsiManager {
    return PsiManager.getInstance(project)
  }

  override fun getTextRange(): TextRange? {
    return TextRange.from(0, 0)
  }
}
