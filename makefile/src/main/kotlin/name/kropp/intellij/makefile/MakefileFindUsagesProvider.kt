package name.kropp.intellij.makefile

import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import name.kropp.intellij.makefile.psi.*
import name.kropp.intellij.makefile.psi.MakefileTypes.*

class MakefileFindUsagesProvider : FindUsagesProvider {
  override fun getWordsScanner() = DefaultWordsScanner(MakefileLexerAdapter(), TokenSet.create(CHARS), TokenSet.create(COMMENT), TokenSet.EMPTY)

  override fun canFindUsagesFor(element: PsiElement) =
    if (element is MakefileTarget) !element.isSpecialTarget
    else element is MakefileNamedElement

  override fun getType(element: PsiElement) = when(element) {
    is MakefileTarget -> if (!element.isSpecialTarget) "Makefile target" else ""
    is MakefileVariable -> "Makefile variable"
    else -> ""
  }

  override fun getDescriptiveName(element: PsiElement): String = if (canFindUsagesFor(element)) { element.text } else ""
  override fun getNodeText(element: PsiElement, useFullName: Boolean): String = if (canFindUsagesFor(element)) { element.text } else ""

  override fun getHelpId(element: PsiElement): String? = null
}