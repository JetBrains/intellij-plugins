package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.language.VueVForExpression

/**
 * @author Irina.Chernushina on 7/28/2017.
 */
class VueJSSpecificHandlersFactory : JavaScriptSpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl?,
                                                 ignorePerformanceLimits: Boolean): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> =
    VueJSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
}

class VueJSReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl?,
                                       ignorePerformanceLimits: Boolean) :
    JSReferenceExpressionResolver(referenceExpression!!, ignorePerformanceLimits) {
  override fun resolve(ref: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> =
    resolveInLocalContext(ref) ?:
    resolveInLocalScript(ref) ?:
    super.resolve(ref, incompleteCode)

  private fun resolveInLocalContext(ref: JSReferenceExpressionImpl): Array<ResolveResult>? {
    if (ref.qualifier != null) return null

    val injectedLanguageManager = InjectedLanguageManager.getInstance(ref.project)
    val host = injectedLanguageManager.getInjectionHost(ref) ?: return null
    val elRef: Ref<PsiElement> = Ref(null)
    var result = false
    PsiTreeUtil.findFirstParent(host, Condition {
      if (it is PsiFile) return@Condition true

      val vForAttribute: XmlAttribute? = (it as? XmlTag)?.getAttribute("v-for")
      if (vForAttribute != null && vForAttribute.valueElement != null) {
        val vFor = PsiTreeUtil.findChildOfType(vForAttribute.valueElement, VueVForExpression::class.java)
        if (vFor != null) {
          val foundVar = vFor.getVarStatement()?.variables?.firstOrNull { it.name == ref.referenceName }
          result = foundVar != null
          elRef.set(foundVar)
        }
      }
      result
    })
    return if (elRef.isNull) {
      null
    }
    else {
      arrayOf(PsiElementResolveResult(elRef.get()))
    }
  }

  fun resolveInLocalScript(ref: JSReferenceExpression): Array<ResolveResult>? {
    ref.referenceName ?: return null
    val scriptWithExport = findScriptWithExport(ref) ?: return null
    val obj = scriptWithExport.second.stubSafeElement as JSObjectLiteralExpression
    val pair = findComponentInnerDetailInObjectLiteral(obj, ref.referenceName!!) ?: return null
    return arrayOf(PsiElementResolveResult(pair.second))
  }
}

fun findScriptWithExport(element : JSReferenceExpression) : Pair<PsiElement, ES6ExportDefaultAssignment>? {
  val xmlFile = (if (element.containingFile is XmlFile) element.containingFile else
    InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)?.containingFile) ?: return null
  val module = org.jetbrains.vuejs.codeInsight.findModule(xmlFile) ?: return null
  val defaultExport = com.intellij.lang.ecmascript6.resolve.ES6PsiUtil.findDefaultExport(module) ?: return null
  if (defaultExport is ES6ExportDefaultAssignment && defaultExport.stubSafeElement is JSObjectLiteralExpression) {
    return Pair(module, defaultExport)
  }
  return null
}