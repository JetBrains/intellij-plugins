package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.CSS_PROPERTIES
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import com.intellij.webSymbols.utils.WebSymbolsStructuredScope
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2Framework
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementVisitor
import org.angular2.lang.html.psi.PropertyBindingType

class HtmlAttributesCustomCssPropertiesScope(location: PsiElement) : WebSymbolsStructuredScope<PsiElement, PsiFile>(location) {

  override val rootPsiElement: PsiFile?
    get() = location.containingFile

  override val scopesBuilderProvider: (PsiFile, WebSymbolsPsiScopesHolder) -> PsiElementVisitor?
    get() = provider@{ file, holder ->
      CustomCssPropertyTemplateScopeBuilder(holder)
    }

  override val providedSymbolKinds: Set<WebSymbolQualifiedKind>
    get() = setOf(CSS_PROPERTIES)

  override fun createPointer(): Pointer<HtmlAttributesCustomCssPropertiesScope> {
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
    fun createCustomCssProperty(attribute: XmlAttribute): AbstractAngular2CustomCssProperty<XmlAttribute>? {
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
    attribute: XmlAttribute,
    info: Angular2AttributeNameParser.PropertyBindingInfo,
  ) : AbstractAngular2CustomCssProperty<XmlAttribute>(attribute, info) {

    override fun getInitialOffset(): Int = 0

    override val valueText: String?
      get() = sourceElement.value

    override fun createPointer(): Pointer<out AbstractAngular2CustomCssProperty<XmlAttribute>> {
      val attributePtr = sourceElement.createSmartPointer()
      return Pointer {
        attributePtr.dereference()?.let { createCustomCssProperty(it) }
      }
    }
  }
}


