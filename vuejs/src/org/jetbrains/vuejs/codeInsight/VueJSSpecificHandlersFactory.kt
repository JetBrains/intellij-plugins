package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.xml.XmlFile

/**
 * @author Irina.Chernushina on 7/28/2017.
 */
class VueJSSpecificHandlersFactory : JavaScriptSpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl?,
                                                 ignorePerformanceLimits: Boolean): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> {
    return VueJSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
  }


}

class VueJSReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl?,
                                       ignorePerformanceLimits: Boolean) :
    JSReferenceExpressionResolver(referenceExpression!!, ignorePerformanceLimits) {
  override fun resolve(ref: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    return resolveInLocalScript(ref) ?: super.resolve(ref, incompleteCode)
  }

  fun resolveInLocalScript(ref: JSReferenceExpression): Array<ResolveResult>? {
    ref.referenceName ?: return null
    val scriptWithExport = findScriptWithExport(ref) ?: return null
    val obj = scriptWithExport.second.stubSafeExpression as JSObjectLiteralExpression
    val pair = findComponentInnerDetailInObjectLiteral(obj, ref.referenceName!!) ?: return null
    return arrayOf(PsiElementResolveResult(pair.second))
  }
}

fun findScriptWithExport(element : JSReferenceExpression) : Pair<PsiElement, ES6ExportDefaultAssignment>? {
  val xmlFile = (if (element.containingFile is XmlFile) element.containingFile else
    InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)?.containingFile) ?: return null
  val module = org.jetbrains.vuejs.codeInsight.findModule(xmlFile) ?: return null
  val defaultExport = com.intellij.lang.ecmascript6.resolve.ES6PsiUtil.findDefaultExport(module) ?: return null
  if (defaultExport is ES6ExportDefaultAssignment && defaultExport.stubSafeExpression is JSObjectLiteralExpression) {
    return Pair(module, defaultExport)
  }
  return null
}