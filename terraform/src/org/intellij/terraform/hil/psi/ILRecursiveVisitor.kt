// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveVisitor

open class ILRecursiveVisitor : ILElementVisitor(),PsiRecursiveVisitor {
  override fun visitElement(element: PsiElement) {
    element.acceptChildren(this)
  }
}