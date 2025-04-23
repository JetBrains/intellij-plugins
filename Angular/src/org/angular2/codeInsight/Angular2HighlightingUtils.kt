// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.validation.JSTooltipWithHtmlHighlighter
import com.intellij.lang.javascript.validation.JSTooltipWithHtmlHighlighter.Companion.applyAttributes
import com.intellij.lang.javascript.validation.JSTooltipWithHtmlHighlighter.Companion.highlightName
import com.intellij.lang.javascript.validation.JSTooltipWithHtmlHighlighter.Companion.highlightWithLexer
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.*
import org.angular2.codeInsight.blocks.Angular2BlockParameterSymbol
import org.angular2.codeInsight.blocks.Angular2HtmlBlockSymbol
import org.angular2.entities.*
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.highlighting.Angular2HighlighterColors
import org.angular2.lang.html.highlighting.Angular2HtmlHighlighterColors
import org.angular2.lang.html.psi.Angular2HtmlBlock

object Angular2HighlightingUtils {

  enum class TextAttributesKind(val key: TextAttributesKey) {
    TS_LOCAL_VARIABLE(TypeScriptHighlighter.TS_LOCAL_VARIABLE),
    TS_PROPERTY(TypeScriptHighlighter.TS_INSTANCE_MEMBER_VARIABLE),
    TS_FUNCTION(TypeScriptHighlighter.TS_EXPORTED_FUNCTION),
    TS_KEYWORD(TypeScriptHighlighter.TS_KEYWORD),
    HTML_TAG(XmlHighlighterColors.HTML_TAG_NAME),
    HTML_ATTRIBUTE(XmlHighlighterColors.HTML_ATTRIBUTE_NAME),
    NG_INPUT(Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME),
    NG_OUTPUT(Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME),
    NG_IN_OUT(Angular2HtmlHighlighterColors.NG_BANANA_BINDING_ATTR_NAME),
    NG_TEMPLATE_VARIABLE(Angular2HighlighterColors.NG_VARIABLE),
    NG_DIRECTIVE(TypeScriptHighlighter.TS_CLASS),
    NG_PIPE(NG_PIPE_KEY),
    NG_EXPORT_AS(NG_EXPORT_AS_KEY),
    NG_BLOCK(Angular2HtmlHighlighterColors.NG_BLOCK_NAME),
    NG_DEFER_TRIGGER(TypeScriptHighlighter.TS_GLOBAL_FUNCTION),
    NG_EXPRESSION_PREFIX(TypeScriptHighlighter.TS_KEYWORD),

    UNUSED(CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES),
    ERROR(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES)
  }

  val NG_EXPORT_AS_KEY: TextAttributesKey = TypeScriptHighlighter.TS_INSTANCE_MEMBER_VARIABLE
  val NG_PIPE_KEY: TextAttributesKey = TypeScriptHighlighter.TS_GLOBAL_VARIABLE

  val Angular2Entity.htmlLabel: String
    get() =
      if (this is Angular2Module && this.isStandalonePseudoModule)
        htmlClassName
      else Angular2Bundle.message(
        when (this) {
          is Angular2Module -> "angular.entity.module"
          is Angular2Component -> "angular.entity.component"
          is Angular2Directive -> "angular.entity.directive"
          is Angular2Pipe -> "angular.entity.pipe"
          else -> throw IllegalStateException(this.javaClass.name)
        }) + " " + htmlClassName


  val Angular2Entity.htmlClassName: String
    get() = highlightName(entitySource ?: sourceElement, entitySourceName)

  val JSClass.htmlName: String
    get() = highlightName(this, name ?: Angular2Bundle.message("angular.description.unknown-class"))

  val Angular2HtmlBlock.htmlName: String
    get() = "@${getName()}".withColor(NG_BLOCK, this)

  fun Angular2HtmlBlockSymbol?.htmlName(context: PsiElement): String =
    "@${this?.name ?: "<unknown>"}".withColor(NG_BLOCK, context)

  fun Angular2BlockParameterSymbol.htmlName(context: PsiElement): String =
    name.withColor(NG_EXPRESSION_PREFIX, context)

  fun String.withNameColor(element: PsiElement): @NlsSafe String =
    highlightName(element, this)

  fun String.withColor(attributes: TextAttributesKind, context: PsiElement, wrapWithCodeTag: Boolean = true): @NlsSafe String =
    applyAttributes(context.project, this, attributes.key, wrapWithCodeTag)

  fun String.withColor(language: Language, context: PsiElement): @NlsSafe String =
    highlightWithLexer(context.project, this, language)

  fun renderCode(vararg items: Pair<String, TextAttributesKind?>, context: PsiElement): String =
    context.project.service<JSTooltipWithHtmlHighlighter>().let { highlighter ->
      highlighter.wrapWithCodeTag(
        items.joinToString("") { (code, key) ->
          if (key != null)
            highlighter.applyAttributes(code, key.key, false)
          else
            code
        }, isBlock = false
      )
    }

  @JvmStatic
  fun <T : Angular2Entity> renderEntityList(entities: Collection<T>): String {
    JSTypeEvaluationLocationProvider.assertLocationIsSet()
    val result = StringBuilder()
    var i = -1
    for (entity in entities) {
      if (++i > 0) {
        if (i == entities.size - 1) {
          result.append(' ')
          result.append(Angular2Bundle.message("angular.description.and-separator"))
          result.append(' ')
        }
        else {
          result.append(", ")
        }
      }
      val sourceElement = entity.sourceElement
      result.append(entity.htmlClassName)
      if (entity is Angular2Pipe) {
        result.append(" (")
        result.append(entity.getName().withColor(NG_PIPE, sourceElement))
        result.append(")")
      }
      else if (entity is Angular2Directive) {
        result.append(" (")
        result.append(highlightWithLexer(sourceElement.project, (entity as Angular2Directive).selector.text,
                                         CSSLanguage.INSTANCE))
        result.append(')')
      }
    }
    return result.toString()
  }
}