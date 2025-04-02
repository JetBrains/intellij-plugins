package org.angular2.web.scopes

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.elementType
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.CSS_PROPERTIES
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.HOST_PROP
import org.angular2.Angular2DecoratorUtil.isHostBinding
import org.angular2.Angular2Framework
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.PropertyBindingType

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
    fun createCustomCssProperty(property: JSProperty): AbstractAngular2CustomCssProperty<JSProperty>? {
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
    property: JSProperty,
    info: Angular2AttributeNameParser.PropertyBindingInfo,
  ) : AbstractAngular2CustomCssProperty<JSProperty>(property, info) {

    override fun getInitialOffset(): Int =
       if (sourceElement.nameIdentifier?.elementType == JSTokenTypes.IDENTIFIER) 0 else 1

    override val valueText: String?
      get() = sourceElement.value?.asSafely<JSLiteralExpression>()?.stringValue

    override fun createPointer(): Pointer<out AbstractAngular2CustomCssProperty<JSProperty>> {
      val propertyPtr = sourceElement.createSmartPointer()
      return Pointer {
        propertyPtr.dereference()?.let { createCustomCssProperty(it) }
      }
    }

  }

}