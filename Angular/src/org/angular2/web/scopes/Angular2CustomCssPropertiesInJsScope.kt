package org.angular2.web.scopes

import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.model.Pointer
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.css.impl.util.CssUtil
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.elementType
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.CSS_PROPERTIES
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.angular2.Angular2DecoratorUtil.HOST_BINDING_DEC
import org.angular2.Angular2DecoratorUtil.getDecoratorForLiteralParameter
import org.angular2.Angular2DecoratorUtil.isHostBinding
import org.angular2.Angular2Framework
import org.angular2.index.Angular2CustomCssPropertyInJsIndexKey
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.PropertyBindingType

class Angular2CustomCssPropertiesInJsScope(file: PsiFile) :
  WebSymbolsScopeWithCache<PsiFile, Unit>(Angular2Framework.ID, file.project, file, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(StubIndex.getInstance().getStubIndexModificationTracker(project))

    val scope = CssUtil.getCompletionAndResolvingScopeForElement(dataHolder)

    StubIndex.getInstance().processAllKeys(
      Angular2CustomCssPropertyInJsIndexKey,
      { name ->
        StubIndex.getInstance().processElements(Angular2CustomCssPropertyInJsIndexKey, name, project, scope,
                                                JSImplicitElementProvider::class.java) {
          when (it) {
            is JSProperty -> createCustomCssProperty(it)?.let(consumer)
            is ES6Decorator -> it.stubSafeCallArguments.firstOrNull()?.asSafely<JSLiteralExpression>()
              ?.let { createCustomCssProperty(it) }
              ?.let(consumer)
          }
          true
        }
      },
      scope
    )
  }

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == CSS_PROPERTIES

  override fun createPointer(): Pointer<Angular2CustomCssPropertiesInJsScope> {
    val filePtr = dataHolder.createSmartPointer()
    return Pointer {
      filePtr.dereference()?.let { Angular2CustomCssPropertiesInJsScope(it) }
    }
  }

  companion object {
    fun createCustomCssProperty(property: JSProperty): AbstractAngular2CustomCssProperty<JSProperty>? {
      if (!isHostBinding(property)) return null
      val info = Angular2AttributeNameParser.parse(property.name ?: return null)
      if (info is Angular2AttributeNameParser.PropertyBindingInfo
          && info.bindingType == PropertyBindingType.STYLE
          && info.name.startsWith("--")
          && info.name.length > 2
      ) {
        return CustomCssPropertyDefinedInHostBinding(property, info)
      }
      return null
    }

    fun createCustomCssProperty(literal: JSLiteralExpression): AbstractAngular2CustomCssProperty<JSLiteralExpression>? {
      if (getDecoratorForLiteralParameter(literal)?.decoratorName != HOST_BINDING_DEC) return null
      val info = Angular2AttributeNameParser.parse(
        literal.significantValue?.let { JSStringUtil.unquoteStringLiteralValue(it) }?.let { "[$it]" }
        ?: return null
      )
      if (info is Angular2AttributeNameParser.PropertyBindingInfo
          && info.bindingType == PropertyBindingType.STYLE
          && info.name.startsWith("--")
          && info.name.length > 2
      ) {
        return CustomCssPropertyDefinedInHostBindingDecorator(literal, info)
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

  private class CustomCssPropertyDefinedInHostBindingDecorator(
    literal: JSLiteralExpression,
    info: Angular2AttributeNameParser.PropertyBindingInfo,
  ) : AbstractAngular2CustomCssProperty<JSLiteralExpression>(literal, info) {

    override fun getInitialOffset(): Int = 0

    override val valueText: String?
      get() = sourceElement.stringValue

    override fun createPointer(): Pointer<out AbstractAngular2CustomCssProperty<JSLiteralExpression>> {
      val literalPtr = sourceElement.createSmartPointer()
      return Pointer {
        literalPtr.dereference()?.let { createCustomCssProperty(it) }
      }
    }

  }

}