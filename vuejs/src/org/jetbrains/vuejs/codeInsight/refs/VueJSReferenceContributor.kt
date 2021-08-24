// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.lang.javascript.patterns.JSPatterns
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
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil.getParentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.VueIdIndex
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.NAME_PROP
import org.jetbrains.vuejs.model.source.TEMPLATE_PROP
import org.jetbrains.vuejs.model.source.VueSourceEntity

class VueJSReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(THIS_INSIDE_COMPONENT, VueComponentLocalReferenceProvider())
    registrar.registerReferenceProvider(COMPONENT_NAME, VueComponentNameReferenceProvider())
    registrar.registerReferenceProvider(TEMPLATE_ID_REF, VueTemplateIdReferenceProvider())
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(JSReferenceExpression::class.java), VueScriptScopeReferenceProvider())
  }

  companion object {
    private val THIS_INSIDE_COMPONENT: ElementPattern<out PsiElement> = createThisInsideComponentPattern()
    private val COMPONENT_NAME: ElementPattern<out PsiElement> = createComponentNamePattern()
    private val TEMPLATE_ID_REF = JSPatterns.jsLiteral()
      .withParent(JSPatterns.jsProperty().withName(TEMPLATE_PROP))

    private fun createThisInsideComponentPattern(): ElementPattern<out PsiElement> {
      return PlatformPatterns.psiElement(JSReferenceExpression::class.java)
        .and(FilterPattern(object : ElementFilter {
          override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
            return VueModelManager.findComponentForThisResolve(
              element.castSafelyTo<JSReferenceExpression>()?.qualifier?.castSafelyTo() ?: return false) != null
          }

          override fun isClassAcceptable(hintClass: Class<*>?): Boolean {
            return true
          }
        }))
    }

    private fun createComponentNamePattern(): ElementPattern<out PsiElement> {
      return PlatformPatterns.psiElement(JSLiteralExpression::class.java)
        .withParent(JSPatterns.jsProperty().withName(NAME_PROP))
        .and(FilterPattern(object : ElementFilter {
          override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
            if (element !is JSElement) return false
            val component = VueModelManager.findEnclosingComponent(element) as? VueSourceEntity ?: return false
            return component.initializer == element.parent?.parent
          }

          override fun isClassAcceptable(hintClass: Class<*>?): Boolean {
            return true
          }

        }))
    }
  }


  private class VueTemplateIdReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
      return if (getTextIfLiteral(element)?.startsWith("#") == true
                 && isVueContext(element)) {
        arrayOf(VueTemplateIdReference(element as JSLiteralExpression, TextRange(2, element.textLength - 1)))
      }
      else emptyArray()
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

  private class VueScriptScopeReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
      if (element !is JSReferenceExpression || element.qualifier != null) return PsiReference.EMPTY_ARRAY
      val name = element.referenceName ?: return PsiReference.EMPTY_ARRAY
      return VueScriptAdditionalScopeProvider.getAdditionalScopeSymbols(element)
        .find { it.name == name }
        ?.let {
          arrayOf(object : PsiReferenceBase<JSReferenceExpression>(element, false) {
            override fun resolve(): PsiElement = it
          })
        }
             ?: PsiReference.EMPTY_ARRAY
    }
  }

  private class VueComponentLocalReference(reference: JSReferenceExpressionImpl,
                                           textRange: TextRange?)
    : CachingPolyReferenceBase<JSReferenceExpressionImpl>(reference, textRange) {

    override fun resolveInner(): Array<ResolveResult> {
      val ref = element
      val name = ref.referenceName
      if (name == null) return ResolveResult.EMPTY_ARRAY
      return ref.qualifier
               .castSafelyTo<JSThisExpression>()
               ?.let { VueModelManager.findComponentForThisResolve(it) }
               ?.thisType
               ?.asRecordType()
               ?.findPropertySignature(name)
               ?.memberSource
               ?.allSourceElements
               ?.mapNotNull { if (it.isValid) PsiElementResolveResult(it) else null }
               ?.toTypedArray<ResolveResult>()
             ?: ResolveResult.EMPTY_ARRAY
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

  private class VueComponentNameReference(element: JSLiteralExpression,
                                          rangeInElement: TextRange?) : CachingPolyReferenceBase<JSLiteralExpression>(element,
                                                                                                                      rangeInElement) {
    override fun resolveInner(): Array<ResolveResult> {
      getParentOfType(element, JSPropertyImpl::class.java, true) ?: return emptyArray()
      return arrayOf(PsiElementResolveResult(JSImplicitElementImpl(element.value.toString(), element)))
    }
  }

  private class VueTemplateIdReference(element: JSLiteralExpression, rangeInElement: TextRange?)
    : CachingPolyReferenceBase<JSLiteralExpression>(element, rangeInElement) {
    override fun resolveInner(): Array<ResolveResult> {
      val result = mutableListOf<ResolveResult>()
      StubIndex.getInstance().processElements(VueIdIndex.KEY, value, element.project,
                                              GlobalSearchScope.projectScope(element.project),
                                              PsiElement::class.java) { element ->
        (element as? XmlAttribute)
          ?.context
          ?.castSafelyTo<XmlTag>()
          ?.let { result.add(PsiElementResolveResult(it)) }
        true
      }
      return result.toTypedArray()
    }
  }

}
