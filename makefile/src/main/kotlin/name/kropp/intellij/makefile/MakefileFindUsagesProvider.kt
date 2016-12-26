package name.kropp.intellij.makefile

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import name.kropp.intellij.makefile.psi.MakefileTarget
import name.kropp.intellij.makefile.psi.MakefileTypes

class MakefileFindUsagesProvider : FindUsagesProvider {
  override fun getWordsScanner() = DefaultWordsScanner(MakefileLexerAdapter(), TokenSet.create(MakefileTypes.IDENTIFIER ), TokenSet.create(MakefileTypes.COMMENT), TokenSet.EMPTY)

  override fun canFindUsagesFor(element: PsiElement) = element is MakefileTarget
  override fun getType(element: PsiElement) = if (element is MakefileTarget) { "Makefile target" } else ""
  override fun getDescriptiveName(element: PsiElement) = if (element is MakefileTarget) { element.text } else ""
  override fun getNodeText(element: PsiElement, useFullName: Boolean) = if (element is MakefileTarget) { element.text } else ""

  override fun getHelpId(p0: PsiElement) = null
}