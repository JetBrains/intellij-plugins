package com.jetbrains.lang.makefile

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.jetbrains.lang.makefile.psi.MakefileNamedElement
import com.jetbrains.lang.makefile.psi.MakefileTarget
import com.jetbrains.lang.makefile.psi.MakefileTypes.CHARS
import com.jetbrains.lang.makefile.psi.MakefileTypes.COMMENT
import com.jetbrains.lang.makefile.psi.MakefileVariable

private class MakefileFindUsagesProvider : FindUsagesProvider {
  override fun getWordsScanner() = DefaultWordsScanner(MakefileLexerAdapter(), TokenSet.create(CHARS), TokenSet.create(COMMENT), TokenSet.EMPTY)

  override fun canFindUsagesFor(element: PsiElement) =
    if (element is MakefileTarget) !element.isSpecialTarget
    else element is MakefileNamedElement

  override fun getType(element: PsiElement) = when(element) {
    is MakefileTarget -> if (!element.isSpecialTarget) MakefileLangBundle.message("usage.type.makefile.target") else ""
    is MakefileVariable -> MakefileLangBundle.message("usage.type.makefile.variable")
    else -> ""
  }

  override fun getDescriptiveName(element: PsiElement): String = if (canFindUsagesFor(element)) { element.text } else ""
  override fun getNodeText(element: PsiElement, useFullName: Boolean): String = if (canFindUsagesFor(element)) { element.text } else ""

  override fun getHelpId(element: PsiElement): String? = null
}