package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.index.VueOptionsIndex
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
    resolveInCurrentComponentDefinition(ref) ?:
    super.resolve(ref, incompleteCode)

  private fun resolveInLocalContext(ref: JSReferenceExpressionImpl): Array<ResolveResult>? {
    if (ref.qualifier != null) return null

    val injectedLanguageManager = InjectedLanguageManager.getInstance(ref.project)
    val host = injectedLanguageManager.getInjectionHost(ref) ?: return null
    val elRef: Ref<PsiElement> = Ref(null)
    PsiTreeUtil.findFirstParent(host, Condition {
      if (it is PsiFile) return@Condition true

      val valueElement = (it as? XmlTag)?.getAttribute("v-for")?.valueElement
      if (valueElement != null) {
        var vFor: VueVForExpression? = PsiTreeUtil.findChildOfType(valueElement, VueVForExpression::class.java)
        if (vFor == null) {
          // jade inside vue, vue injected inside js embedded in jade
          val embeddedJS = PsiTreeUtil.findChildOfType(valueElement, JSEmbeddedContent::class.java)
          val lookForInjectedInside : PsiElement = embeddedJS?.firstChild as? JSLiteralExpression ?: valueElement
          vFor = injectedLanguageManager.getInjectedPsiFiles(lookForInjectedInside)
            ?.map { PsiTreeUtil.findChildOfType(it.first, VueVForExpression::class.java) }?.firstOrNull()
        }
        if (vFor != null) {
          val foundVar = vFor.getVarStatement()?.variables?.firstOrNull { it.name == ref.referenceName }
          elRef.set(foundVar)
          return@Condition foundVar != null
        }
      }
      false
    })
    return if (elRef.isNull) {
      null
    }
    else {
      arrayOf(PsiElementResolveResult(elRef.get()))
    }
  }

  fun resolveInCurrentComponentDefinition(ref: JSReferenceExpression): Array<ResolveResult>? {
    ref.referenceName ?: return null
    val obj = findScriptWithExport(ref)?.second?.stubSafeElement as? JSObjectLiteralExpression
              ?: findInMountedVueInstance(ref) ?: return null
    val pair = findComponentInnerDetailInObjectLiteral(obj, ref.referenceName!!) ?: return null
    return arrayOf(PsiElementResolveResult(pair.second))
  }
}

fun findInMountedVueInstance(reference : JSReferenceExpression) : JSObjectLiteralExpression? {
  val xmlHost = InjectedLanguageManager.getInstance(reference.project).getInjectionHost(reference) ?: return null
  val ref : Ref<JSObjectLiteralExpression?> = Ref(null)
  PsiTreeUtil.findFirstParent(xmlHost, Condition {
    if (it is PsiFile) return@Condition true
    val idValue = (it as? XmlTag)?.getAttribute("id")?.valueElement?.value ?: return@Condition false
    if (!StringUtil.isEmptyOrSpaces(idValue)) {
      val element = org.jetbrains.vuejs.index.resolve("#" + idValue, GlobalSearchScope.projectScope(reference.project), VueOptionsIndex.KEY)
      val obj = element as? JSObjectLiteralExpression ?: PsiTreeUtil.getParentOfType(element, JSObjectLiteralExpression::class.java)
      ref.set(obj)
      return@Condition obj != null
    }
    false
  })
  return ref.get()
}

fun findScriptWithExport(element : JSReferenceExpression) : Pair<PsiElement, ES6ExportDefaultAssignment>? {
  val xmlFile = getContainingXmlFile(element) ?: return null
  val module = org.jetbrains.vuejs.codeInsight.findModule(xmlFile) ?: return null
  val defaultExport = com.intellij.lang.ecmascript6.resolve.ES6PsiUtil.findDefaultExport(module) ?: return null
  if (defaultExport is ES6ExportDefaultAssignment && defaultExport.stubSafeElement is JSObjectLiteralExpression) {
    return Pair(module, defaultExport)
  }
  return null
}

private fun getContainingXmlFile(element: JSReferenceExpression) =
  (element.containingFile as? XmlFile ?:
   InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)?.containingFile as? XmlFile)