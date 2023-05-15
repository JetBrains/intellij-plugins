package org.intellij.prisma.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.util.SmartList
import org.intellij.prisma.lang.psi.PrismaNamedElement

open class PrismaProcessor : PsiScopeProcessor {
  private val variants = SmartList<PrismaNamedElement>()

  var multiple = true
  var filter: ((PsiElement) -> Boolean)? = null

  override fun execute(element: PsiElement, state: ResolveState): Boolean {
    if (element !is PrismaNamedElement) {
      return true
    }
    if (accepts(element) && filter?.invoke(element) != false) {
      variants.add(element)
      return multiple
    }
    return true
  }

  open fun accepts(element: PsiElement): Boolean {
    return true
  }

  fun getResults(): List<PrismaNamedElement> {
    return variants
  }
}
