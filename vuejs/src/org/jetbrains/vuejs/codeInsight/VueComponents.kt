package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.ES6QualifiedNameResolver
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.index.getVueIndexData

/**
 * @author Irina.Chernushina on 9/26/2017.
 *
 * Basic resolve from index here (when we have the name literal and the descriptor literal/reference)
 */
class VueComponents {
  companion object {
    fun onlyLocal(elements: Collection<JSImplicitElement>): List<JSImplicitElement> {
      return elements.filter(this::isNotInLibrary)
    }

    fun isNotInLibrary(element : JSImplicitElement): Boolean {
      val file = element.containingFile.viewProvider.virtualFile
      return !JSProjectUtil.isInLibrary(file, element.project) && !JSLibraryUtil.isProbableLibraryFile(file)
    }

    fun findComponentDescriptor(element: JSImplicitElement): JSObjectLiteralExpression? {
      val parent = element.parent
      if (parent is JSCallExpression) {
        val reference = getVueIndexData(element).descriptorRef ?: return null
        return resolveReferenceToObjectLiteral(element, reference)
      }
      return (parent as? JSProperty)?.context as? JSObjectLiteralExpression
    }

    fun vueMixinDescriptorFinder(implicitElement: JSImplicitElement): JSObjectLiteralExpression? {
      val typeString = getVueIndexData(implicitElement).descriptorRef
      if (!StringUtil.isEmptyOrSpaces(typeString)) {
        val expression = resolveReferenceToObjectLiteral(implicitElement, typeString!!)
        if (expression != null) {
          return expression
        }
      }
      val mixinObj = (implicitElement.parent as? JSProperty)?.parent as? JSObjectLiteralExpression
      if (mixinObj != null) return mixinObj

      val call = implicitElement.parent as? JSCallExpression
      if (call != null) {
        return JSStubBasedPsiTreeUtil.findDescendants(call, JSStubElementTypes.OBJECT_LITERAL_EXPRESSION)
          .firstOrNull { (it.context as? JSArgumentList)?.context == call || (it.context == call) }
      }
      return null
    }

    private fun resolveReferenceToObjectLiteral(element: JSImplicitElement, reference: String): JSObjectLiteralExpression? {
      val scope = createLocalResolveScope(element)

      val resolvedLocally = JSStubBasedPsiTreeUtil.resolveLocally(reference, scope)
      if (resolvedLocally != null) {
        return getLiteralFromResolve(listOf(resolvedLocally))
      }

      val elements = ES6QualifiedNameResolver(scope).resolveQualifiedName(reference)
      return getLiteralFromResolve(elements)
    }

    private fun createLocalResolveScope(element: PsiElement): PsiElement =
      PsiTreeUtil.getContextOfType(element, JSCatchBlock::class.java, JSClass::class.java, JSExecutionScope::class.java)
      ?: element.containingFile

    private fun getLiteralFromResolve(result: Collection<PsiElement>): JSObjectLiteralExpression? {
      return result.mapNotNull(fun(it: PsiElement): JSObjectLiteralExpression? {
        val element: PsiElement? = (it as? JSVariable)?.initializerOrStub ?: it
        if (element is JSObjectLiteralExpression) return element
        return JSStubBasedPsiTreeUtil.calculateMeaningfulElement(element!!) as? JSObjectLiteralExpression
      }).firstOrNull()
    }
  }
}