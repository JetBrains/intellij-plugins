// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
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
import com.intellij.psi.util.PsiTreeUtil.isAncestor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.castSafelyTo
import org.apache.commons.lang.StringUtils
import org.jetbrains.vuejs.codeInsight.findScriptWithExport
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.VueIdIndex
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelProximityVisitor
import org.jetbrains.vuejs.model.VueProperty
import org.jetbrains.vuejs.model.source.NAME_PROP
import org.jetbrains.vuejs.model.source.TEMPLATE_PROP

class VueJSReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(FUNCTION_INSIDE_SCRIPT, VueComponentLocalReferenceProvider())
    registrar.registerReferenceProvider(COMPONENT_NAME, VueComponentNameReferenceProvider())
    registrar.registerReferenceProvider(TEMPLATE_ID_REF, VueTemplateIdReferenceProvider())
  }

  companion object {
    private val FUNCTION_INSIDE_SCRIPT: ElementPattern<out PsiElement> = createFunctionInsideScript()
    private val COMPONENT_NAME: ElementPattern<out PsiElement> = createComponentName()
    private val TEMPLATE_ID_REF = JSPatterns.jsLiteralExpression()
      .withParent(JSPatterns.jsProperty().withName(TEMPLATE_PROP))

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
          return ((element.parent as? JSPropertyImpl)?.name == NAME_PROP && defaultExport as PsiElement == element.parent.parent.parent)
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
      getParentOfType(element, JSFunction::class.java, true) ?: return emptyArray()
      // let function context around the expression be enough to think it is used somewhere in assembling the exported object
      return resolveInCurrentComponentDefinition(element)
             ?: emptyArray()
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
