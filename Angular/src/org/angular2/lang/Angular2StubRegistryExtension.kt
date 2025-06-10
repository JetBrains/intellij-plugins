package org.angular2.lang

import com.intellij.lang.javascript.stubs.register
import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import org.angular2.lang.expr.parser.FILE
import org.angular2.lang.html.psi.impl.Angular2HtmlBananaBoxBindingImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlEventImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlLetImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlPropertyBindingImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlReferenceImpl
import org.angular2.lang.html.psi.impl.Angular2HtmlTemplateBindingsImpl
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes.*
import org.angular2.lang.stubs.*
import org.angular2.lang.stubs.Angular2HtmlAttributeStubFactory
import org.angular2.lang.stubs.Angular2HtmlAttributeStubSerializer

class Angular2StubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    FILE.let {
      registry.registerStubSerializer(it, JSFileStubSerializer(it.language))
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