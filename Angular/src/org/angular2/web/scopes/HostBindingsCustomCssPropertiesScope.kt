package org.angular2.web.scopes

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.elementType
import com.intellij.refactoring.rename.api.RenameValidator
import com.intellij.util.asSafely
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.CSS_PROPERTIES
import com.intellij.webSymbols.documentation.WebSymbolDocumentation
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.HOST_PROP
import org.angular2.Angular2DecoratorUtil.isHostBinding
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.web.Angular2SymbolOrigin
import org.angular2.web.scopes.HtmlAttributesCustomCssPropertiesScope.CustomCssPropertyNameValidator
import org.jetbrains.annotations.Nls

class HostBindingsCustomCssPropertiesScope(decorator: ES6Decorator) :
  WebSymbolsScopeWithCache<ES6Decorator, Unit>(Angular2Framework.ID, decorator.project, decorator, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(dataHolder)
    Angular2DecoratorUtil.getProperty(dataHolder, HOST_PROP)
      ?.value?.asSafely<JSObjectLiteralExpression>()?.properties?.forEach {
        createCustomCssProperty(it)?.let { consumer(it) }
      }
  }

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == CSS_PROPERTIES

  override fun createPointer(): Pointer<out WebSymbolsScopeWithCache<ES6Decorator, Unit>> {
    val decoratorPtr = dataHolder.createSmartPointer()
    return Pointer {
      decoratorPtr.dereference()?.let { HostBindingsCustomCssPropertiesScope(it) }
    }
  }

  companion object {
    fun createCustomCssProperty(property: JSProperty): WebSymbolDeclaredInPsi? {
      if (!isHostBinding(property)) return null
      val info = Angular2AttributeNameParser.parse(property.name ?: return null)
      if (info is Angular2AttributeNameParser.PropertyBindingInfo
          && info.bindingType == PropertyBindingType.STYLE
          && info.name.startsWith("--")
          && info.name.length > 2) {
        return CustomCssPropertyDefinedInHostBinding(property, info)
      }
      return null
    }
  }


  private class CustomCssPropertyDefinedInHostBinding(
    private val property: JSProperty,
    info: Angular2AttributeNameParser.PropertyBindingInfo,
  ) : WebSymbolDeclaredInPsi {

    override val sourceElement: PsiElement
      get() = property

    override val textRangeInSourceElement: TextRange? =
      info.nameOffset
        .let { if (property.nameIdentifier?.elementType == JSTokenTypes.IDENTIFIER) it else it + 1 }
        .let { offset -> TextRange(offset, offset + info.name.length)}

    override fun createPointer(): Pointer<out WebSymbolDeclaredInPsi> {
      val propertyPtr = property.createSmartPointer()
      return Pointer {
        propertyPtr.dereference()?.let { createCustomCssProperty(it) }
      }
    }

    override val origin: WebSymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val namespace: @NlsSafe SymbolNamespace
      get() = CSS_PROPERTIES.namespace

    override val kind: @NlsSafe SymbolKind
      get() = CSS_PROPERTIES.kind

    override val name: @NlsSafe String = info.name

    override fun validator(): RenameValidator =
      CustomCssPropertyNameValidator

    override val descriptionSections: Map<@Nls String, @Nls String>
      get() = property.value?.asSafely<JSLiteralExpression>()?.stringValue
                ?.withColor(Angular2Language, property)
                ?.let { mapOf("Value" to it) }
              ?: emptyMap()

    override fun createDocumentation(location: PsiElement?): WebSymbolDocumentation? =
      WebSymbolDocumentation.create(
        this,
        location,
        definition = "css property ".withColor(TextAttributesKind.TS_KEYWORD, property) + name.withColor(TextAttributesKind.CSS_PROPERTY, property)
      )

    override fun equals(other: Any?): Boolean =
      other === this ||
      other is CustomCssPropertyDefinedInHostBinding
      && other.property == property

    override fun hashCode(): Int =
      property.hashCode()

  }

}