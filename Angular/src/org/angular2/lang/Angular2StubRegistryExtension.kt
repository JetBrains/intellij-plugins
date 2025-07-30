package org.angular2.lang

import com.intellij.lang.javascript.stubs.register
import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.BANANA_BOX_BINDING
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.EVENT
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.LET
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.NG_CONTENT_SELECTOR
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.PROPERTY_BINDING
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.REFERENCE
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.TEMPLATE_BINDINGS
import org.angular2.lang.html.psi.impl.*
import org.angular2.lang.stubs.*

class Angular2StubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    for (type in Angular2TemplateSyntax.entries.map { it.expressionLanguageFileElementType }.distinct()) {
      registry.registerStubSerializer(type, JSFileStubSerializer(type.language))
    }

    listOf(
      Angular2StringPartsLiteralExpressionStubFactory(),
      Angular2StringPartsLiteralExpressionStubSerializer(),
    ).forEach(registry::register)

    registry.registerStubFactory(NG_CONTENT_SELECTOR, Angular2HtmlNgContentSelectorStubFactory())
    registry.registerStubSerializer(NG_CONTENT_SELECTOR, Angular2HtmlNgContentSelectorStubSerializer())

    listOf(
      Angular2HtmlAttributeStubFactory(EVENT, ::Angular2HtmlEventImpl),
      Angular2HtmlAttributeStubSerializer(EVENT),

      Angular2HtmlAttributeStubFactory(BANANA_BOX_BINDING, ::Angular2HtmlBananaBoxBindingImpl),
      Angular2HtmlAttributeStubSerializer(BANANA_BOX_BINDING),

      Angular2HtmlAttributeStubFactory(PROPERTY_BINDING, ::Angular2HtmlPropertyBindingImpl),
      Angular2HtmlAttributeStubSerializer(PROPERTY_BINDING),

      Angular2HtmlAttributeStubFactory(REFERENCE, ::Angular2HtmlReferenceImpl),
      Angular2HtmlAttributeStubSerializer(REFERENCE),

      Angular2HtmlAttributeStubFactory(LET, ::Angular2HtmlLetImpl),
      Angular2HtmlAttributeStubSerializer(LET),

      Angular2HtmlAttributeStubFactory(TEMPLATE_BINDINGS, ::Angular2HtmlTemplateBindingsImpl),
      Angular2HtmlAttributeStubSerializer(TEMPLATE_BINDINGS),
    ).forEach {
      when (it) {
        is Angular2HtmlAttributeStubFactory -> registry.registerStubFactory(it.elementType, it)
        is Angular2HtmlAttributeStubSerializer -> registry.registerStubSerializer(it.elementType, it)
      }
    }
  }
}