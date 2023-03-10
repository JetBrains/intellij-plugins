// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.impl

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.javascript.web.js.jsType
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.lang.javascript.documentation.*
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.lang.typescript.documentation.TypeScriptDocumentationProvider
import com.intellij.model.Pointer
import com.intellij.navigation.TargetPresentation
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import com.intellij.util.ui.UIUtil
import com.intellij.webSymbols.WebSymbol
import org.angular2.entities.*
import org.angular2.entities.source.Angular2SourceDirectiveProperty
import org.jetbrains.annotations.Nls

class Angular2ElementDocumentationTarget private constructor(
  @NlsSafe val name: String,
  val elements: List<Angular2Element>
) : DocumentationTarget {

  override fun createPointer(): Pointer<out DocumentationTarget> {
    val name = this.name
    val elementPointers = this.elements.map { it.createPointer() }
    return Pointer {
      create(name, *elementPointers.mapNotNull { it.dereference() }.toTypedArray())
    }
  }

  override fun computePresentation(): TargetPresentation {
    return TargetPresentation.builder(name).presentation()
  }

  @Suppress("HardCodedStringLiteral")
  override fun computeDocumentation(): DocumentationResult =
    elements.joinToString("\n<hr>\n") { Angular2ElementDocumentation(it).build() }
      .let { DocumentationResult.documentation(it) }

  private data class Angular2ElementDocumentation(val element: Angular2Element) {

    fun build(): @Nls String {
      val source = when (element) {
        is Angular2Entity -> element.typeScriptClass
        is Angular2SourceDirectiveProperty -> element.sources.find { hasNonPrivateDocComment(it) }
        else -> element.sourceElement.takeIf { it !is TypeScriptClass }
      }
      return buildDefinition() + Angular2ElementDocProvider.renderDocComment(source)
    }

    private fun buildDefinition(): @Nls String {
      val kindName = when (element) {
        is Angular2DirectiveAttribute -> "directive attribute"
        is Angular2DirectiveProperty -> "directive property"
        is Angular2Component -> "component"
        is Angular2Directive -> "directive"
        is Angular2Pipe -> "pipe"
        is Angular2Module -> "module"
        else -> throw UnsupportedOperationException(element::class.java.name)
      }
      val result = StringBuilder()
      val scheme = EditorColorsManager.getInstance().globalScheme

      fun appendSpanStart(color: TextAttributesKey) {
        result
          .append("<span style='color: #")
        UIUtil.appendColor(scheme.getAttributes(color).foregroundColor, result)
        result
          .append("'>")
      }

      fun appendSpanEnd() {
        result.append("</span>")
      }

      result.append(DocumentationMarkup.DEFINITION_START)

      appendSpanStart(TypeScriptHighlighter.TS_KEYWORD)
      result.append(kindName)
      appendSpanEnd()
      result.append(" ")

      if (element is Angular2Entity) {
        appendSpanStart(TypeScriptHighlighter.TS_CLASS)
        result.append(element.getName())
        appendSpanEnd()
      }
      else if (element is WebSymbol) {
        appendSpanStart(TypeScriptHighlighter.TS_INSTANCE_MEMBER_VARIABLE)
        result.append(element.name)
        appendSpanEnd()
        element.jsType?.let {
          result.append(
            JSHtmlHighlightingUtil.getElementHtmlHighlighting(element.sourceElement, CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, it)
              .replace(Regex("<span[^>]*>${CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED}</span>"), "")
              .replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, ""))
        }
      }
      @Suppress("HardCodedStringLiteral")
      return result.append(DocumentationMarkup.DEFINITION_END).toString()
    }

  }

  object Angular2ElementDocProvider : TypeScriptDocumentationProvider() {

    override fun createDocumentationBuilder(element: PsiElement, contextElement: PsiElement?): JSDocumentationBuilder {
      return Angular2ElementDocumentationBuilder(element, contextElement, this)
    }

    fun renderDocComment(source: PsiElement?): @Nls String {
      val docComment =
        source
          ?.let { JSDocumentationUtils.findDocComment(it) }
          ?.asSafely<JSDocComment>()
          ?.takeIf { it.tags.none { tag -> "docs-private" == tag.name } }
        ?: return ""

      return generateRenderedDoc(docComment) ?: ""
    }
  }

  class Angular2ElementDocumentationBuilder(element: PsiElement, contextElement: PsiElement?, provider: JSDocumentationProvider)
    : JSDocumentationBuilder(element, contextElement, provider) {

    override fun createSymbolInfoPrinter(target: JSDocSymbolInfoBuilder,
                                         element: PsiElement,
                                         contextElement: PsiElement?): JSDocSimpleInfoPrinter<*> {
      return Angular2SymbolInfoPrinter(target, element, contextElement, true)
    }

    override fun createMethodInfoPrinter(target: JSDocMethodInfoBuilder,
                                            functionItem: JSFunctionItem,
                                            element: PsiElement,
                                            contextElement: PsiElement?): JSDocSimpleInfoPrinter<*> {
      return Angular2SymbolInfoPrinter(target, element, contextElement, true)
    }

  }

  class Angular2SymbolInfoPrinter<T : JSDocSymbolInfoBuilder>(
    builder: T, element: PsiElement, contextElement: PsiElement?, canBeNamed: Boolean)
    : JSDocSymbolInfoPrinter<T>(builder, element, contextElement, canBeNamed) {

    override fun appendInnerSections(result: java.lang.StringBuilder, provider: JSDocumentationProvider, hasDefinition: Boolean) {
      super.appendInnerSections(result, provider, false)
      appendLocation(result)
    }
  }

  companion object {

    fun create(name: String, vararg elements: Angular2Element?): Angular2ElementDocumentationTarget? {
      return Angular2ElementDocumentationTarget(
        name, elements.mapNotNull { it }.ifEmpty { return null })
    }

    private fun hasNonPrivateDocComment(element: PsiElement): Boolean {
      val comment = JSDocumentationUtils.findDocComment(element)
      return comment is JSDocComment && comment.tags.none { tag -> "docs-private" == tag.name }
    }
  }


}