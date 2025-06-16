package org.angular2.web.scopes

import com.intellij.css.frontback.icons.CssFrontbackApiIcons
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.css.impl.CssNamedItemPresentation
import com.intellij.psi.css.impl.util.CssUtil
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.css.CSS_PROPERTIES
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.polySymbols.css.properties.AbstractCssCustomPropertySymbolDeclaredInPsi
import org.angular2.Angular2DecoratorUtil.HOST_BINDING_DEC
import org.angular2.Angular2DecoratorUtil.getDecoratorForLiteralParameter
import org.angular2.Angular2DecoratorUtil.isHostBinding
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.index.Angular2CustomCssPropertyInHtmlAttributeIndexKey
import org.angular2.index.Angular2CustomCssPropertyInJsIndexKey
import org.angular2.isCustomCssPropertyBinding
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute
import org.jetbrains.annotations.Nls

class Angular2CustomCssPropertiesScope(file: PsiFile) :
  PolySymbolScopeWithCache<PsiFile, Unit>(Angular2Framework.ID, file.project, file, Unit) {

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
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

    StubIndex.getInstance().processAllKeys(
      Angular2CustomCssPropertyInHtmlAttributeIndexKey,
      { name ->
        StubIndex.getInstance().processElements(Angular2CustomCssPropertyInHtmlAttributeIndexKey, name, project, scope,
                                                Angular2HtmlBoundAttribute::class.java) {
          createCustomCssProperty(it)?.let(consumer)
          true
        }
      },
      scope
    )
  }

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == CSS_PROPERTIES

  override fun createPointer(): Pointer<Angular2CustomCssPropertiesScope> {
    val filePtr = dataHolder.createSmartPointer()
    return Pointer {
      filePtr.dereference()?.let { Angular2CustomCssPropertiesScope(it) }
    }
  }

  companion object {
    fun createCustomCssProperty(property: JSProperty): AbstractCssCustomPropertySymbolDeclaredInPsi<JSProperty>? {
      if (!isHostBinding(property)) return null
      val info = Angular2AttributeNameParser.parse(property.name ?: return null)
      if (isCustomCssPropertyBinding(info)) {
        return CustomCssPropertyDefinedInHostBinding(property, info)
      }
      return null
    }

    fun createCustomCssProperty(literal: JSLiteralExpression): AbstractCssCustomPropertySymbolDeclaredInPsi<JSLiteralExpression>? {
      if (getDecoratorForLiteralParameter(literal)?.decoratorName != HOST_BINDING_DEC) return null
      val info = Angular2AttributeNameParser.parse(
        literal.significantValue?.let { JSStringUtil.unquoteStringLiteralValue(it) }?.let { "[$it]" }
        ?: return null
      )
      if (isCustomCssPropertyBinding(info)) {
        return CustomCssPropertyDefinedInHostBindingDecorator(literal, info)
      }
      return null
    }

    fun createCustomCssProperty(attribute: XmlAttribute): AbstractCssCustomPropertySymbolDeclaredInPsi<XmlAttribute>? {
      val info = Angular2AttributeNameParser.parse(attribute.name)
      if (isCustomCssPropertyBinding(info)) {
        return CustomCssPropertyDefinedInHtmlAttribute(attribute, info)
      }
      return null
    }
  }


  private abstract class AbstractAngular2CustomCssProperty<T : PsiElement>(
    sourceElement: T,
    info: Angular2AttributeNameParser.PropertyBindingInfo,
  ) : AbstractCssCustomPropertySymbolDeclaredInPsi<T>(sourceElement) {

    abstract fun getInitialOffset(): Int

    abstract val valueText: String?

    abstract override fun createPointer(): Pointer<out AbstractCssCustomPropertySymbolDeclaredInPsi<T>>

    final override val textRangeInSourceElement: TextRange? =
      getInitialOffset().let { TextRange(info.nameOffset + it, info.nameOffset + it + info.name.length) }

    final override val origin: PolySymbolOrigin
      get() = PolySymbolOrigin.empty()

    final override val name: @NlsSafe String = info.name

    final override val descriptionSections: Map<@Nls String, @Nls String>
      get() = valueText
                ?.withColor(Angular2Language, sourceElement)
                ?.let { mapOf("Value" to it) }
              ?: emptyMap()

    @Suppress("HardCodedStringLiteral")
    override val presentation: TargetPresentation =
      TargetPresentation.builder(name.removePrefix("--"))
        .icon(CssFrontbackApiIcons.Custom_property)
        .containerText(CssNamedItemPresentation.getLocationString(sourceElement))
        .presentation()

  }

  private class CustomCssPropertyDefinedInHostBinding(
    property: JSProperty,
    info: Angular2AttributeNameParser.PropertyBindingInfo,
  ) : AbstractAngular2CustomCssProperty<JSProperty>(property, info) {

    override fun getInitialOffset(): Int =
      if (sourceElement.nameIdentifier?.elementType == JSTokenTypes.IDENTIFIER) 0 else 1

    override val valueText: String?
      get() = sourceElement.value?.asSafely<JSLiteralExpression>()?.stringValue

    override fun createPointer(): Pointer<out AbstractCssCustomPropertySymbolDeclaredInPsi<JSProperty>> {
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

    override fun createPointer(): Pointer<out AbstractCssCustomPropertySymbolDeclaredInPsi<JSLiteralExpression>> {
      val literalPtr = sourceElement.createSmartPointer()
      return Pointer {
        literalPtr.dereference()?.let { createCustomCssProperty(it) }
      }
    }

  }

  private class CustomCssPropertyDefinedInHtmlAttribute(
    attribute: XmlAttribute,
    info: Angular2AttributeNameParser.PropertyBindingInfo,
  ) : AbstractAngular2CustomCssProperty<XmlAttribute>(attribute, info) {

    override fun getInitialOffset(): Int = 0

    override val valueText: String?
      get() = sourceElement.value

    override fun createPointer(): Pointer<out AbstractCssCustomPropertySymbolDeclaredInPsi<XmlAttribute>> {
      val literalPtr = sourceElement.createSmartPointer()
      return Pointer {
        literalPtr.dereference()?.let { createCustomCssProperty(it) }
      }
    }
  }
}