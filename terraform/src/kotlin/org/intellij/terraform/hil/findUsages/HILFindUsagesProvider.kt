// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.findUsages

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.intellij.terraform.hil.psi.HILLexer
import org.intellij.terraform.hil.psi.ILProperty
import org.intellij.terraform.hil.psi.ILVariable

class HILFindUsagesProvider : FindUsagesProvider {
  override fun getWordsScanner(): WordsScanner {
    return HILWordsScanner(HILLexer())
  }

  override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
    return psiElement is ILVariable || psiElement is ILProperty
  }

  override fun getHelpId(psiElement: PsiElement): String? {
    return null
  }

  override fun getType(element: PsiElement): String {
    if (element is ILVariable)
      return "hil.variable"
    if (element is ILProperty)
      return "hil.property"
    return ""
  }

  override fun getDescriptiveName(element: PsiElement): String {
    val name = when (element) {
      is PsiNamedElement -> element.name
      is NavigatablePsiElement -> element.name
      else -> null
    }
    return name ?: "<Not An PsiNamedElement/NavigatablePsiElement ${element.node.elementType}>"
  }

  override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
    return getDescriptiveName(element)
  }
}
