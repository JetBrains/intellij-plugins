// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import org.apache.commons.lang.StringUtils
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.expr.VueVForExpression
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelProximityVisitor
import org.jetbrains.vuejs.model.VueProperty

class VueJSReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl?,
                                       ignorePerformanceLimits: Boolean) :
  JSReferenceExpressionResolver(referenceExpression!!, ignorePerformanceLimits) {
  override fun resolve(ref: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> =
    resolveInLocalContext(ref) ?: resolveInCurrentComponentDefinition(ref) ?: super.resolve(ref, incompleteCode)

  private fun resolveInLocalContext(ref: JSReferenceExpressionImpl): Array<ResolveResult>? {
    if (ref.qualifier != null) return null

    val injectedLanguageManager = InjectedLanguageManager.getInstance(ref.project)
    // injection host can be xml attribute value or embedded js inside jade tag - this we just skip moving up
    val host = injectedLanguageManager.getInjectionHost(ref)
    val elRef: Ref<PsiElement> = Ref(null)
    PsiTreeUtil.findFirstParent(host ?: ref, Condition {
      if (it is PsiFile) return@Condition true

      val valueElement = (it as? XmlTag)?.getAttribute("v-for")?.valueElement ?: return@Condition false
      // vue v-for embedded in attribute value in vue file & html template language
      val vFor = getCachedVForInsideAttribute(valueElement, injectedLanguageManager) ?: return@Condition false
      elRef.set(vFor.getVarStatement()?.variables?.firstOrNull { jsVariable -> jsVariable.name == ref.referenceName })
      return@Condition !elRef.isNull
    })
    val foundElement = elRef.get() ?: return null
    return arrayOf(PsiElementResolveResult(foundElement))
  }

  private fun getCachedVForInsideAttribute(valueElement: XmlAttributeValue,
                                           injectedLanguageManager: InjectedLanguageManager): VueVForExpression? {
    // <template lang="jade">, vue injected inside js string embedded in jade tag
    // or vue injected into xml attribute value directly - in html
    return CachedValuesManager.getCachedValue(valueElement, CachedValueProvider {
      var vFor = PsiTreeUtil.findChildOfType(valueElement,
                                             VueVForExpression::class.java)
      if (vFor == null) {
        var lookForInjectedInside: PsiElement = valueElement
        if (HTMLLanguage.INSTANCE != valueElement.language) {
          val embeddedJS = PsiTreeUtil.findChildOfType(valueElement,
                                                       JSEmbeddedContent::class.java)
          val literal = embeddedJS?.firstChild as? JSLiteralExpression
          if (literal != null) {
            lookForInjectedInside = literal
          }
        }
        vFor = injectedLanguageManager.getInjectedPsiFiles(lookForInjectedInside)
          ?.map {
            PsiTreeUtil.findChildOfType(it.first, VueVForExpression::class.java)
          }?.firstOrNull()
      }
      return@CachedValueProvider CachedValueProvider.Result(vFor, valueElement)
    })
  }

  fun resolveInCurrentComponentDefinition(ref: JSReferenceExpression): Array<ResolveResult>? {
    if (ref.qualifier != null && ref.qualifier !is JSThisExpression) return null
    val name = StringUtils.uncapitalize(ref.referenceName) ?: return null
    val result = mutableListOf<ResolveResult>()
    VueModelManager.findEnclosingContainer(ref)?.acceptPropertiesAndMethods(object : VueModelProximityVisitor() {
      override fun visitProperty(property: VueProperty, proximity: Proximity): Boolean {
        return acceptSameProximity(proximity, name == StringUtils.uncapitalize(property.name)) {
          property.source?.let { result.add(PsiElementResolveResult(it)) }
        }
      }

      override fun visitMethod(method: VueMethod, proximity: Proximity): Boolean {
        return acceptSameProximity(proximity, name == StringUtils.uncapitalize(method.name)) {
          method.source?.let { result.add(PsiElementResolveResult(it)) }
        }
      }
    }, onlyPublic = false)
    return if (result.isNotEmpty()) result.toTypedArray() else null
  }
}

fun findScriptWithExport(element: PsiElement): Pair<PsiElement, ES6ExportDefaultAssignment>? {
  val xmlFile = getContainingXmlFile(element) ?: return null

  val module = findModule(xmlFile) ?: return null
  val defaultExport = com.intellij.lang.ecmascript6.resolve.ES6PsiUtil.findDefaultExport(module)
                        as? ES6ExportDefaultAssignment ?: return null
  if (defaultExport.stubSafeElement is JSObjectLiteralExpression) {
    return Pair(module, defaultExport)
  }
  return null
}

fun getContainingXmlFile(element: PsiElement): XmlFile? =
  (element.containingFile as? XmlFile
   ?: element as? XmlFile
   ?: InjectedLanguageManager.getInstance(
     element.project).getInjectionHost(element)?.containingFile as? XmlFile)
