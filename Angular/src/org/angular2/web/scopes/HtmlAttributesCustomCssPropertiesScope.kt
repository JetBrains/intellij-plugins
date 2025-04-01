package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.CSS_PROPERTIES
import com.intellij.webSymbols.documentation.WebSymbolDocumentation
import com.intellij.webSymbols.query.WebSymbolsCompoundScope
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import com.intellij.webSymbols.utils.WebSymbolsStructuredScope
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementVisitor
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.web.Angular2SymbolOrigin
import org.jetbrains.annotations.Nls

class HtmlAttributesCustomCssPropertiesScope(location: PsiElement) : WebSymbolsStructuredScope<PsiElement, PsiFile>(location) {

  override val rootPsiElement: PsiFile?
    get() = location.containingFile

  override val scopesBuilderProvider: (PsiFile, WebSymbolsPsiScopesHolder) -> PsiElementVisitor?
    get() = provider@{ file, holder ->
      CustomCssPropertyTemplateScopeBuilder(holder)
    }

  override val providedSymbolKinds: Set<WebSymbolQualifiedKind>
    get() = setOf(CSS_PROPERTIES)

  override fun createPointer(): Pointer<out WebSymbolsCompoundScope> {
    val locationPtr = location.createSmartPointer()
    return Pointer {
      val location = locationPtr.dereference() ?: return@Pointer null
      HtmlAttributesCustomCssPropertiesScope(location)
    }
  }

  private class CustomCssPropertyTemplateScopeBuilder(
    private val holder: WebSymbolsPsiScopesHolder,
  ) : Angular2HtmlRecursiveElementVisitor() {

    override fun visitFile(file: PsiFile) {
      Angular2SourceUtil.findComponentClass(file)
        ?.let { Angular2DecoratorUtil.findDecorator(it, COMPONENT_DEC) }
        ?.let { HostBindingsCustomCssPropertiesScope(it) }
        ?.getSymbols(CSS_PROPERTIES,
                     WebSymbolsListSymbolsQueryParams.create(
                       WebSymbolsQueryExecutorFactory.createCustom()
                         .setFramework(Angular2Framework.ID)
                         .allowResolve(true)
                         .create(), false),
                     Stack())
        ?.filterIsInstance<WebSymbol>()
        ?.forEach { symbol -> holder.currentScope { addSymbol(symbol) } }
      super.visitFile(file)
    }

    override fun visitXmlTag(tag: XmlTag) {
      holder.pushScope(tag)
      try {
        super.visitXmlTag(tag)
      }
      finally {
        holder.popScope()
      }
    }

    override fun visitXmlAttribute(attribute: XmlAttribute) {
      createCustomCssProperty(attribute)?.let { holder.currentScope { addSymbol(it) } }
    }

  }

  companion object {
    fun createCustomCssProperty(attribute: XmlAttribute): WebSymbolDeclaredInPsi? {
      val info = Angular2AttributeNameParser.parse(attribute.name)
      if (info is Angular2AttributeNameParser.PropertyBindingInfo
          && info.bindingType == PropertyBindingType.STYLE
          && info.name.startsWith("--")
          && info.name.length > 2) {
        return CustomCssPropertyDefinedInAttribute(attribute, info)
      }
      return null
    }
  }

  private class CustomCssPropertyDefinedInAttribute(
    private val attribute: XmlAttribute,
    info: Angular2AttributeNameParser.PropertyBindingInfo,
  ) : WebSymbolDeclaredInPsi {

    override val sourceElement: PsiElement
      get() = attribute

    override val textRangeInSourceElement: TextRange? = TextRange(info.nameOffset, info.nameOffset + info.name.length)

    override fun createPointer(): Pointer<out WebSymbolDeclaredInPsi> {
      val attributePtr = attribute.createSmartPointer()
      return Pointer {
        attributePtr.dereference()?.let { createCustomCssProperty(it) }
      }
    }

    override val origin: WebSymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val namespace: @NlsSafe SymbolNamespace
      get() = CSS_PROPERTIES.namespace

    override val kind: @NlsSafe SymbolKind
      get() = CSS_PROPERTIES.kind

    override val name: @NlsSafe String = info.name

    override val descriptionSections: Map<@Nls String, @Nls String>
      get() = attribute.value?.withColor(Angular2Language, attribute)?.let {
        mapOf("Value" to it)
      } ?: emptyMap()

    override fun createDocumentation(location: PsiElement?): WebSymbolDocumentation? =
      WebSymbolDocumentation.create(
        this,
        location,
        definition = "css property ".withColor(TextAttributesKind.TS_KEYWORD, attribute) + name.withColor(TextAttributesKind.CSS_PROPERTY, attribute)
      )

    override fun equals(other: Any?): Boolean =
      other === this ||
      other is CustomCssPropertyDefinedInAttribute
      && other.attribute == attribute

    override fun hashCode(): Int =
      attribute.hashCode()

  }

}