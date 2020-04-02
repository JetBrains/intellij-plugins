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
import com.intellij.psi.util.PsiTreeUtil.*
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.castSafelyTo
import org.apache.commons.lang.StringUtils
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.VueIdIndex
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelProximityVisitor
import org.jetbrains.vuejs.model.VueProperty
import org.jetbrains.vuejs.model.source.NAME_PROP
import org.jetbrains.vuejs.model.source.TEMPLATE_PROP
import org.jetbrains.vuejs.model.source.VueSourceEntity

class VueJSReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(THIS_INSIDE_COMPONENT, VueComponentLocalReferenceProvider())
    registrar.registerReferenceProvider(COMPONENT_NAME, VueComponentNameReferenceProvider())
    registrar.registerReferenceProvider(TEMPLATE_ID_REF, VueTemplateIdReferenceProvider())
  }

  companion object {
    private val THIS_INSIDE_COMPONENT: ElementPattern<out PsiElement> = createThisInsideComponentPattern()
    private val COMPONENT_NAME: ElementPattern<out PsiElement> = createComponentNamePattern()
    private val TEMPLATE_ID_REF = JSPatterns.jsLiteralExpression()
      .withParent(JSPatterns.jsProperty().withName(TEMPLATE_PROP))

    private fun createThisInsideComponentPattern(): ElementPattern<out PsiElement> {
      return PlatformPatterns.psiElement(JSReferenceExpression::class.java)
        .and(FilterPattern(object : ElementFilter {
          override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
            if (element !is JSReferenceExpression
                || element.qualifier !is JSThisExpression) return false

            //find enclosing function and it's second level enclosing function
            val function = getContextOfType(element, JSFunction::class.java)
            if (function == null || !isVueContext(function)) return false
            var secondLevelFunctionScope = findFirstContext(function, true) { it is JSFunction && !it.isArrowFunction } as? JSFunction
            if (function.isArrowFunction && secondLevelFunctionScope != null) {
              secondLevelFunctionScope = findFirstContext(secondLevelFunctionScope,
                                                          true) { it is JSFunction && !it.isArrowFunction } as? JSFunction
            }

            val component = VueModelManager.findEnclosingComponent(element) as? VueSourceEntity ?: return false

            // we're good if function is part of implementation and second level function is null or is not part of the component
            return component.isPartOfImplementation(function)
                   && (secondLevelFunctionScope == null || !component.isPartOfImplementation(secondLevelFunctionScope))
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


  private class VueComponentLocalReference(reference: JSReferenceExpressionImpl,
                                           textRange: TextRange?) : CachingPolyReferenceBase<JSReferenceExpressionImpl>(reference,
                                                                                                                        textRange) {
    override fun resolveInner(): Array<ResolveResult> {
      // let function context around the expression be enough to think it is used somewhere in assembling the exported object
      val ref = element
      val name = StringUtils.uncapitalize(ref.referenceName)
      if (name == null
          || (ref.qualifier != null && ref.qualifier !is JSThisExpression)
          || (getParentOfType(element, JSFunction::class.java, true)) == null) return ResolveResult.EMPTY_ARRAY
      val result = mutableListOf<ResolveResult>()
      VueModelManager.findEnclosingComponent(ref)?.acceptPropertiesAndMethods(object : VueModelProximityVisitor() {
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
      return if (result.isNotEmpty()) result.toTypedArray() else ResolveResult.EMPTY_ARRAY
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
