package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.CachingPolyReferenceBase
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.util.PsiTreeUtil.getParentOfType
import com.intellij.psi.util.PsiTreeUtil.isAncestor
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.VueFileType

/**
 * @author Irina.Chernushina on 8/1/2017.
 */
class VueJSReferenceContributor : PsiReferenceContributor() {
  companion object {
    private val FUNCTION_INSIDE_SCRIPT: ElementPattern<out PsiElement> = createFunctionInsideScript()
    private val COMPONENT_NAME: ElementPattern<out PsiElement> = createComponentName()

    private fun createFunctionInsideScript(): ElementPattern<out PsiElement> {
      return PlatformPatterns.psiElement(JSReferenceExpression::class.java)
        .and(FilterPattern(object : ElementFilter {
          override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
            if (element !is PsiElement) return false
            val function = getParentOfType(element, JSFunction::class.java) ?: return false

            if (element !is JSReferenceExpression || element.qualifier !is JSThisExpression) return false

            val pair = findScriptWithExport(element) ?: return false
            // lexical this for arrow functions => must be in-place
            val isArrowFunction = function is JSFunctionExpression && function.isArrowFunction
            return isAncestor(pair.first, element, true) && (!isArrowFunction || isAncestor(pair.second, element, true))
          }

          override fun isClassAcceptable(hintClass: Class<*>?): Boolean {
            return true
          }
        }))
    }

    private fun createComponentName(): ElementPattern<out PsiElement> {
      return PlatformPatterns.psiElement(JSLiteralExpression::class.java).and(FilterPattern(object : ElementFilter {
        override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
          if (element !is PsiElement) return false
          if (element.containingFile.fileType != VueFileType.INSTANCE) return false
          val content = findModule(element) ?: return false
          val defaultExport = ES6PsiUtil.findDefaultExport(content)
          if (defaultExport == null || element.parent.parent.parent == null) return false
          return ((element.parent as? JSPropertyImpl)?.name == "name" && defaultExport as PsiElement == element.parent.parent.parent)
        }

        override fun isClassAcceptable(hintClass: Class<*>?): Boolean {
          return true
        }

      }))
    }
  }

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(FUNCTION_INSIDE_SCRIPT, VueComponentLocalReferenceProvider())
    registrar.registerReferenceProvider(COMPONENT_NAME, VueComponentNameReferenceProvider())
  }
}

private class VueComponentLocalReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (element is JSReferenceExpressionImpl) {
      return arrayOf(VueComponentLocalReference(element, ElementManipulators.getValueTextRange(element)))
    }
    return emptyArray()
  }
}


private class VueComponentLocalReference(reference: JSReferenceExpressionImpl,
                                         textRange: TextRange?) : CachingPolyReferenceBase<JSReferenceExpressionImpl>(reference,
                                                                                                                      textRange) {
  override fun resolveInner(): Array<ResolveResult> {
    getParentOfType(element, JSFunction::class.java, true) ?: return emptyArray()
    // let function context around the expression be enough to think it is used somewhere in assembling the exported object
    return VueJSReferenceExpressionResolver(element, false).resolveInCurrentComponentDefinition(element)
           ?: emptyArray()
  }
}

private class VueComponentNameReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (element is JSLiteralExpression) {
      return arrayOf(VueComponentNameReference(element, ElementManipulators.getValueTextRange(element)))
    }
    return emptyArray()
  }

}

private class VueComponentNameReference(reference: JSLiteralExpression,
                                        textRange: TextRange?) : CachingPolyReferenceBase<JSLiteralExpression>(reference, textRange) {
  override fun resolveInner(): Array<ResolveResult> {
    getParentOfType(element, JSPropertyImpl::class.java, true) ?: return emptyArray()
    return arrayOf(PsiElementResolveResult(JSImplicitElementImpl(element.value.toString(), element)))
  }
}
