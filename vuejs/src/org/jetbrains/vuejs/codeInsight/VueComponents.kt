package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.ES6QualifiedNameResolver
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author Irina.Chernushina on 9/26/2017.
 */
class VueComponents {
  companion object {
    fun selectComponent(elements : Collection<JSImplicitElement>?, ignoreLibraries: Boolean) : JSImplicitElement? {
      elements ?: return null
      var filtered : Collection<JSImplicitElement> = onlyLocal(elements)
      if (filtered.isEmpty()) {
        if (ignoreLibraries) return null
        filtered = elements
      }

      return filtered.firstOrNull { it.typeString != null } ?: elements.firstOrNull()
    }

    fun onlyLocal(elements: Collection<JSImplicitElement>): List<JSImplicitElement> {
      return elements.filter {
        val file = it.containingFile.viewProvider.virtualFile
        !JSProjectUtil.isInLibrary(file, it.project) && !JSLibraryUtil.isProbableLibraryFile(file)
      }
    }

    fun findComponentDescriptor(element: JSImplicitElement): JSObjectLiteralExpression? {
      val parent = element.parent

      if (parent is JSCallExpression) {
        val reference = element.typeString ?: return null

        val scope = PsiTreeUtil.getContextOfType(element, JSCatchBlock::class.java, JSClass::class.java, JSExecutionScope::class.java)
          ?: element.containingFile

        val resolvedLocally = JSStubBasedPsiTreeUtil.resolveLocally(reference, scope)
        if (resolvedLocally != null) {
          return getLiteralFromResolve(listOf(resolvedLocally))
        }

        val elements = ES6QualifiedNameResolver(scope).resolveQualifiedName(reference)
        return getLiteralFromResolve(elements)
      }
      return (parent as? JSProperty)?.context as? JSObjectLiteralExpression
    }

    fun getLiteralFromResolve(result : Collection<PsiElement>): JSObjectLiteralExpression? {
      return result.mapNotNull {
        it as? JSObjectLiteralExpression ?:
        JSStubBasedPsiTreeUtil.calculateMeaningfulElement(it) as? JSObjectLiteralExpression
      }.firstOrNull()
    }

    fun isGlobal(it: JSImplicitElement) = it.typeString != null
  }
}