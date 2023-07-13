// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.*
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.NAME_PROP
import org.angular2.Angular2DecoratorUtil.PIPE_DEC
import org.angular2.Angular2DecoratorUtil.STYLE_URLS_PROP
import org.angular2.Angular2DecoratorUtil.VIEW_CHILDREN_DEC
import org.angular2.Angular2DecoratorUtil.VIEW_CHILD_DEC
import org.angular2.Angular2DecoratorUtil.isAngularEntityDecorator
import org.angular2.Angular2DecoratorUtil.isLiteralInNgDecorator
import org.angular2.lang.Angular2LangUtil
import org.angularjs.codeInsight.refs.AngularJSTemplateReferencesProvider

internal class Angular2TSReferencesContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(STYLE_PATTERN, Angular2StyleUrlsReferencesProvider())
    registrar.registerReferenceProvider(VIEW_CHILD_PATTERN, Angular2ViewChildReferencesProvider())
    registrar.registerReferenceProvider(PIPE_NAME_PATTERN, Angular2PipeNameReferencesProvider())
  }

  class Angular2StyleUrlsReferencesProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
      return AngularJSTemplateReferencesProvider.Angular2SoftFileReferenceSet(element).allReferences
    }
  }

  private val PIPE_NAME_PATTERN = ng2LiteralInDecoratorProperty(NAME_PROP, PIPE_DEC)
  private val VIEW_CHILD_PATTERN = PlatformPatterns.psiElement(JSLiteralExpression::class.java).and(FilterPattern(object : ElementFilter {
    override fun isAcceptable(element: Any, context: PsiElement?): Boolean {
      return element.asSafely<JSLiteralExpression>()
               ?.takeIf { it.isQuotedLiteral }
               ?.parent?.asSafely<JSArgumentList>()
               ?.parent?.asSafely<JSCallExpression>()
               ?.parent?.asSafely<ES6Decorator>()
               ?.let { decorator -> isAngularEntityDecorator(decorator, VIEW_CHILD_DEC, VIEW_CHILDREN_DEC) }
             ?: false
    }

    override fun isClassAcceptable(hintClass: Class<*>): Boolean {
      return true
    }
  }))

  private val STYLE_PATTERN = PlatformPatterns.psiElement(JSLiteralExpression::class.java).and(FilterPattern(object : ElementFilter {
    override fun isAcceptable(element: Any, context: PsiElement?): Boolean {
      return element.asSafely<JSLiteralExpression>()
               ?.takeIf { it.isQuotedLiteral }
               ?.parent?.asSafely<JSArrayLiteralExpression>()
               ?.parent?.asSafely<JSProperty>()
               ?.let { property ->
                 STYLE_URLS_PROP == property.name && Angular2LangUtil.isAngular2Context(property)
               }
             ?: false
    }

    override fun isClassAcceptable(hintClass: Class<*>): Boolean {
      return true
    }
  }))

  private fun ng2LiteralInDecoratorProperty(propertyName: String,
                                            vararg decoratorNames: String): PsiElementPattern.Capture<JSLiteralExpression> {
    return PlatformPatterns.psiElement(JSLiteralExpression::class.java).and(FilterPattern(object : ElementFilter {
      override fun isAcceptable(element: Any, context: PsiElement?): Boolean {
        return element is PsiElement && isLiteralInNgDecorator(element, propertyName, *decoratorNames)
      }

      override fun isClassAcceptable(hintClass: Class<*>): Boolean {
        return true
      }
    }))
  }
}
