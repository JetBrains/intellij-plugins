// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.JSInjectionBracesUtil
import com.intellij.lang.javascript.injections.JSFormattableInjectionUtil
import com.intellij.lang.javascript.injections.JSInjectionUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlText
import com.intellij.util.NullableFunction
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.HOST_PROP
import org.angular2.Angular2DecoratorUtil.STYLES_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP
import org.angular2.Angular2DecoratorUtil.VIEW_DEC
import org.angular2.Angular2DecoratorUtil.isAngularEntityDecorator
import org.angular2.cli.config.AngularConfigProvider
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.parser.Angular2PsiParser.Companion.ACTION
import org.angular2.lang.expr.parser.Angular2PsiParser.Companion.BINDING
import org.angular2.lang.expr.parser.Angular2PsiParser.Companion.INTERPOLATION
import org.angular2.lang.expr.parser.Angular2PsiParser.Companion.SIMPLE_BINDING
import org.angular2.lang.expr.parser.Angular2PsiParser.Companion.TEMPLATE_BINDINGS
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.parser.Angular2AttributeNameParser

class Angular2Injector : MultiHostInjector {
  internal object Holder {
    val BRACES_FACTORY: NullableFunction<PsiElement, Pair<String, String>> = JSInjectionBracesUtil
      .delimitersFactory(Angular2HtmlLanguage.displayName) { _, _ -> null }/* no support for custom delimiters*/
  }

  override fun elementsToInjectIn(): List<Class<out PsiElement>> {
    return listOf(JSLiteralExpression::class.java, XmlText::class.java)
  }

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    val parent = context.parent
    if (parent == null
        || parent.language.`is`(Angular2Language)
        || parent.language.isKindOf(Angular2HtmlLanguage)
        || !Angular2LangUtil.isAngular2Context(context)) {
      return
    }

    if (context is XmlText) {
      injectInterpolations(registrar, context)
      return
    }

    if (!(context is JSLiteralExpression && context.isQuotedLiteral)) {
      return
    }

    if (isPropertyWithName(parent, TEMPLATE_PROP)) {
      injectIntoDecoratorExpr(registrar, context, parent, Angular2LangUtil.getTemplateSyntax(context).language, null)
      return
    }

    val grandParent = parent.parent
    if ((parent is JSArrayLiteralExpression && isPropertyWithName(grandParent, STYLES_PROP))
        || isPropertyWithName(parent, STYLES_PROP)) {
      injectIntoDecoratorExpr(registrar, context, grandParent, getCssDialect(context), null)
      return
    }

    if (injectIntoEmbeddedLiteral(registrar, context, parent)) {
      return
    }

    if (parent is JSProperty && parent.parent is JSObjectLiteralExpression) {
      val ancestor = parent.parent.parent
      if (isPropertyWithName(ancestor, HOST_PROP)) {
        val name = parent.name ?: return
        val fileExtension = getExpressionFileExtension(context.textLength, name, true)
                            ?: return
        injectIntoDecoratorExpr(registrar, context, ancestor, Angular2Language, fileExtension)
      }
    }
  }

  private fun getCssDialect(literalExpression: JSLiteralExpression): Language {
    val file = literalExpression.containingFile.originalFile

    return CachedValuesManager.getCachedValue(file) {
      val angularConfig = AngularConfigProvider.findAngularConfig(file.project, file.virtualFile)

      if (angularConfig == null) {
        return@getCachedValue create<Language>(CSSLanguage.INSTANCE, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
      }

      var cssDialect: Language = CSSLanguage.INSTANCE
      val angularProject = angularConfig.getProject(file.virtualFile)
      if (angularProject != null) {
        val projectCssDialect = angularProject.inlineStyleLanguage
        if (projectCssDialect != null) {
          cssDialect = projectCssDialect
        }
      }
      create(cssDialect, angularConfig.file, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }
  }

  private fun injectIntoEmbeddedLiteral(registrar: MultiHostRegistrar,
                                        context: JSLiteralExpression,
                                        parent: PsiElement): Boolean {
    val attribute = parent.asSafely<JSEmbeddedContent>()
                      ?.let { PsiTreeUtil.getParentOfType(it, XmlAttribute::class.java) }
                    ?: return false
    val expressionType: String? = getExpressionFileExtension(context.textLength, attribute.name, false)
    if (expressionType != null) {
      inject(registrar, context, Angular2Language, expressionType)
    }
    else {
      injectInterpolations(registrar, context)
    }
    return true
  }

  private fun injectInterpolations(registrar: MultiHostRegistrar, context: PsiElement) {
    val braces = Holder.BRACES_FACTORY.`fun`(context) ?: return
    JSInjectionBracesUtil.injectInXmlTextByDelimiters(registrar, context, Angular2Language,
                                                      braces.first, braces.second, INTERPOLATION)
  }

  private fun getExpressionFileExtension(valueLength: Int, attributeName: String, hostBinding: Boolean): String? {
    if (valueLength == 0) {
      return null
    }
    val normalized = if (!hostBinding)
      Angular2AttributeNameParser.normalizeAttributeName(attributeName)
    else attributeName
    return when {
      normalized.startsWith("(") && normalized.endsWith(")") || !hostBinding && normalized.startsWith("on-") -> {
        ACTION
      }
      normalized.startsWith("[") && normalized.endsWith("]") -> {
        if (hostBinding) SIMPLE_BINDING else BINDING
      }
      !hostBinding && (normalized.startsWith("bind-") || normalized.startsWith("bindon-")) -> {
        BINDING
      }
      !hostBinding && normalized.startsWith("*") -> {
        normalized.substring(1) + "." + TEMPLATE_BINDINGS
      }
      else -> null
    }
  }

  private fun injectIntoDecoratorExpr(registrar: MultiHostRegistrar,
                                      context: JSLiteralExpression,
                                      ancestor: PsiElement,
                                      language: Language,
                                      fileExtension: String?) {
    val decorator = PsiTreeUtil.getContextOfType(ancestor, ES6Decorator::class.java)
    if (decorator != null) {
      if (isAngularEntityDecorator(decorator, true, COMPONENT_DEC, DIRECTIVE_DEC, VIEW_DEC)) {
        inject(registrar, context, language, fileExtension)
        JSFormattableInjectionUtil.setReformattableInjection(context, language)
      }
    }
  }


  private fun inject(registrar: MultiHostRegistrar, context: JSLiteralExpression, language: Language,
                     extension: String?) {
    JSInjectionUtil.injectInQuotedLiteral(registrar, language, extension, context, null, null)
  }

  private fun isPropertyWithName(element: PsiElement, requiredName: String): Boolean {
    return element is JSProperty && element.name == requiredName
  }
}
