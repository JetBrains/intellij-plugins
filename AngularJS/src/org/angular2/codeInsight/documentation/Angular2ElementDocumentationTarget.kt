// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.documentation

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol
import com.intellij.javascript.web.js.WebJSTypesUtil.jsGenericType
import com.intellij.javascript.webSymbols.jsType
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.lang.javascript.documentation.*
import com.intellij.lang.javascript.documentation.JSDocSimpleInfoPrinter.addSections
import com.intellij.lang.javascript.documentation.JSHtmlHighlightingUtil.STYLE_BOLD
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.jsdoc.JSDocBlockTags
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.lang.typescript.documentation.TypeScriptDocumentationProvider
import com.intellij.model.Pointer
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.ui.ColorUtil
import com.intellij.util.applyIf
import com.intellij.util.asSafely
import com.intellij.util.containers.toMultiMap
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.webSymbols.WebSymbol
import org.angular2.entities.*
import org.angular2.entities.source.Angular2SourceDirectiveProperty
import org.angular2.lang.types.BindingsTypeResolver
import org.angular2.web.Angular2WebSymbolsQueryConfigurator
import org.jetbrains.annotations.Nls

class Angular2ElementDocumentationTarget private constructor(
  @NlsSafe val name: String,
  val location: PsiElement?,
  val elements: List<Angular2Element>,
) : DocumentationTarget {

  fun withDirective(directive: Angular2Directive): Angular2ElementDocumentationTarget =
    Angular2ElementDocumentationTarget(name, location, elements.filter { it !is Angular2Directive } + directive)

  override fun createPointer(): Pointer<out DocumentationTarget> {
    val name = this.name
    val locationPtr = location?.createSmartPointer()
    val elementPointers = this.elements.map { it.createPointer() }
    return Pointer {
      val location = locationPtr?.let { it.dereference() ?: return@Pointer null }
      Angular2ElementDocumentationTarget(name, location, elementPointers.mapNotNull { it.dereference() })
    }
  }

  override fun computePresentation(): TargetPresentation {
    return TargetPresentation.builder(name).presentation()
  }

  @Suppress("HardCodedStringLiteral")
  override fun computeDocumentation(): DocumentationResult {
    val directive = elements.firstNotNullOfOrNull { it as? Angular2Directive }
    if (elements.size == 1)
      return DocumentationResult.documentation(Angular2ElementDocumentation(elements[0], location, directive).build())
    val result = StringBuilder()
    var module: String? = null
    for (i in elements.indices) {
      val doc = Angular2ElementDocumentation(elements[i], location, directive).build()
      if (i + 1 < elements.size) {
        moduleRegex.find(doc)?.value?.let { module = it }
        val adjustedSections = doc
          .replace(moduleRegex, "")
          .replace("<table class='sections'></table>", "")
        val addSeparator = adjustedSections.contains("<div class='content")
                           || adjustedSections.contains(DocumentationMarkup.SECTIONS_START)
        if (addSeparator) result.append("<div class='separated'>")
        result.append(adjustedSections)
        if (addSeparator) result.append("</div>")
      }
      else {
        result.append(doc)
        if (!moduleRegex.containsMatchIn(doc) && module != null) {
          result.append(DocumentationMarkup.SECTIONS_START)
          result.append(module)
          result.append(DocumentationMarkup.SECTIONS_END)
        }
      }
      result.append("\n")
    }
    if (!result.contains(DocumentationMarkup.SECTIONS_START) && !result.contains(DocumentationMarkup.CONTENT_START)) {
      var prevIndex = result.lastIndexOf(DocumentationMarkup.DEFINITION_START)
      var curIndex = result.lastIndexOf(DocumentationMarkup.DEFINITION_START, prevIndex - 1)
      while (curIndex >= 0) {
        result.insert(prevIndex, "</div>")
        result.insert(curIndex, "<div class='separated'>")
        prevIndex = curIndex
        curIndex = result.lastIndexOf(DocumentationMarkup.DEFINITION_START, prevIndex - 1)
      }
    }
    return DocumentationResult.documentation(result.toString())
  }

  private data class Angular2ElementDocumentation(val element: Angular2Element,
                                                  val location: PsiElement?,
                                                  val directive: Angular2Directive?) {
    fun build(): @Nls String {
      val source = when (element) {
        is Angular2Entity -> element.typeScriptClass
        is Angular2SourceDirectiveProperty -> element.sources.find { hasNonPrivateDocComment(it) }
        is Angular2DirectiveExportAs -> null
        else -> element.sourceElement.takeIf { it !is TypeScriptClass }
      }
      return (buildDefinition() + Angular2ElementDocProvider(buildAdditionalSections()).renderDocComment(source))
        .applyIf(element is Angular2Entity) {
          // remove self links
          val link = Regex.escape(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL + (element as Angular2Entity).className)
          replace(Regex("<span\\s+style='[^']*'><a\\s+href=['\"]$link['\"]\\s*>(.*?)</a\\s*></span>"), "$1")
        }
    }

    private fun buildAdditionalSections(): List<Pair<String, String>> {
      val result = mutableListOf<Pair<String, String>>()
      when (val element = element) {
        is Angular2Directive -> {
          if (element.isStandalone) {
            result.add("Standalone" to "")
          }
          element.selector.simpleSelectors.mapTo(result) {
            Pair("Selectors",
                 SyntaxPrinter(element.sourceElement)
                   .withPre { append(it.toString(), CSSLanguage.INSTANCE) }
                   .toString()
            )
          }
          element.exportAs.values
            .filter { it.directive == element }
            .mapTo(result) {
              Pair("Export as", SyntaxPrinter(element.sourceElement).withPre {
                append(TypeScriptHighlighter.TS_INSTANCE_MEMBER_VARIABLE, it.name)
              }.toString())
            }
          element.hostDirectives.mapNotNullTo(result) { hostDirective ->
            hostDirective.directive?.typeScriptClass?.let { cls ->
              Pair("Host directives", SyntaxPrinter(cls).withPre { append(cls.jsType) }.toString())
            }
          }
        }
      }
      return result
    }

    private fun buildDefinition(): @Nls String {
      val kindName = when (element) {
        is Angular2DirectiveAttribute -> "directive attribute"
        is Angular2DirectiveProperty -> when (element.kind) {
          Angular2WebSymbolsQueryConfigurator.KIND_NG_DIRECTIVE_INPUTS -> "directive input"
          Angular2WebSymbolsQueryConfigurator.KIND_NG_DIRECTIVE_OUTPUTS -> "directive output"
          Angular2WebSymbolsQueryConfigurator.KIND_NG_DIRECTIVE_IN_OUTS -> "directive inout"
          else -> "directive property"
        }
        is Angular2Component -> "component"
        is Angular2Directive -> "directive"
        is Angular2Pipe -> "pipe"
        is Angular2Module -> "module"
        else -> throw UnsupportedOperationException(element::class.java.name)
      }
      val result = SyntaxPrinter(element.sourceElement)
      result.appendRaw(DocumentationMarkup.DEFINITION_START)
        .append(TypeScriptHighlighter.TS_KEYWORD, kindName)
        .appendRaw(" ")

      val bindingsTypeResolver: BindingsTypeResolver? = BindingsTypeResolver.get(location)
      if (element is Angular2Entity) {
        val jsType = element.typeScriptClass?.jsGenericType?.let {
          bindingsTypeResolver?.substituteTypeForDocumentation(directive, it) ?: it
        }
        if (jsType != null) {
          result.append(jsType)
        }
        else {
          result.append(TypeScriptHighlighter.TS_CLASS, element.getName())
        }
      }
      else if (element is WebSymbol) {
        result.append(TypeScriptHighlighter.TS_INSTANCE_MEMBER_VARIABLE, element.name)
        val jsType = bindingsTypeResolver?.substituteTypeForDocumentation(directive, element.jsType)
                     ?: element.jsType
        jsType?.let {
          result.append(TypeScriptHighlighter.TS_OPERATION_SIGN, ": ")
          result.append(it)
        }
      }
      @Suppress("HardCodedStringLiteral")
      return result.appendRaw(DocumentationMarkup.DEFINITION_END).toString()
    }

  }

  private class SyntaxPrinter(private val context: PsiElement) {
    private val builder: StringBuilder = StringBuilder()
    private val scheme = EditorColorsManager.getInstance().globalScheme
    private val linkColor by lazy(LazyThreadSafetyMode.NONE) { ColorUtil.toHtmlColor(JBUI.CurrentTheme.Link.Foreground.ENABLED) }

    fun withColor(color: TextAttributesKey?, action: SyntaxPrinter.() -> Unit): SyntaxPrinter {
      builder.append("<span style='color: #")
      UIUtil.appendColor(scheme.getAttributes(color)?.foregroundColor ?: scheme.defaultForeground, builder)
      builder.append("'>")
      this.action()
      builder.append("</span>")
      return this
    }

    fun withPre(action: SyntaxPrinter.() -> Unit): SyntaxPrinter {
      builder.append("<pre>")
      this.action()
      builder.append("</pre>")
      return this
    }

    fun appendRaw(text: String): SyntaxPrinter {
      builder.append(text)
      return this
    }

    fun append(color: TextAttributesKey, text: String): SyntaxPrinter =
      withColor(color) {
        builder.append(StringUtil.escapeXmlEntities(text))
      }

    fun append(text: String, language: Language): SyntaxPrinter {
      builder.append(HtmlSyntaxInfoUtil.getHtmlContent(
        PsiFileFactory.getInstance(context.project).createFileFromText(language, text),
        text,
        null,
        EditorColorsManager.getInstance().globalScheme,
        0,
        text.length
      )?.toString()?.replace(STYLE_BOLD, "") ?: text)
      return this
    }

    fun append(jsType: JSType): SyntaxPrinter {
      JSHtmlHighlightingUtil.getElementHtmlHighlighting(context, CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, jsType)
        .toString()
        .replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "")
        .replaceFirst(Regex(":&#32;"), "")
        .replace(Regex("<span[^>]*></span>"), "")
        .replace(Regex("(<a href=[^>]*>(.*?)</a\\s*>)"), "<span style='color: $linkColor'>$1</span>")
        .let {
          withColor(null) { // default editor color might differ
            builder.append(it)
          }
        }
      return this
    }

    override fun toString(): String =
      builder.toString()
  }

  class Angular2ElementDocProvider(internal val additionalSections: List<Pair<String, String>>) : TypeScriptDocumentationProvider() {

    override fun createDocumentationBuilder(element: PsiElement, contextElement: PsiElement?): JSDocumentationBuilder {
      return Angular2ElementDocumentationBuilder(element, contextElement, this)
    }

    fun renderDocComment(source: PsiElement?): @Nls String {
      val docComment = source
        ?.let { JSDocumentationUtils.findDocComment(it) }
        ?.asSafely<JSDocComment>()
        ?.takeIf { it.tags.none { tag -> "docs-private" == tag.name } }
      if (docComment != null) {
        return generateRenderedDoc(docComment) ?: ""
      }
      else if (additionalSections.isNotEmpty()) {
        @Nls
        val result = StringBuilder()
        result.append(DocumentationMarkup.SECTIONS_START)
        addSections(additionalSections.toMultiMap(), result)
        result.append(DocumentationMarkup.SECTIONS_END)
        return result.toString()
      }
      else return ""
    }
  }

  class Angular2ElementDocumentationBuilder(element: PsiElement, contextElement: PsiElement?, provider: JSDocumentationProvider)
    : JSDocumentationBuilder(element, contextElement, provider) {

    init {
      (provider as Angular2ElementDocProvider).additionalSections.forEach {
        addSection(it.first, it.second)
      }
    }

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

    private val moduleRegex = Regex("<tr><td valign='top'( colspan='2')?><icon src='JavaScriptPsiIcons\\.FileTypes\\.[^']+'/>[^<]+</td>")

    private val docsPrivate = JSDocBlockTags.definitionFor("docs-private")

    fun create(name: String, location: PsiElement?, vararg elements: Angular2Element?): Angular2ElementDocumentationTarget? {
      return Angular2ElementDocumentationTarget(
        name, location, elements.mapNotNull { it }.ifEmpty { return null })
    }

    private fun hasNonPrivateDocComment(element: PsiElement): Boolean {
      val comment = JSDocumentationUtils.findDocComment(element)
      return comment is JSDocComment && comment.findTags(docsPrivate).isEmpty()
    }
  }


}