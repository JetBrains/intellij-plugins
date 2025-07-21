// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs

import com.intellij.lang.javascript.stubs.register
import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.lang.stubs.HtmlStubBasedTagStubSerializer
import com.intellij.lang.stubs.XmlStubBasedAttributeStubFactory
import com.intellij.lang.stubs.XmlStubBasedAttributeStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import org.jetbrains.vuejs.lang.expr.parser.VUEJS_FILE
import org.jetbrains.vuejs.lang.expr.parser.VUETS_FILE
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.EMBEDDED_EXPR_CONTENT_JS
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.EMBEDDED_EXPR_CONTENT_TS
import org.jetbrains.vuejs.lang.html.VueFileElementType
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.REF_ATTRIBUTE
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.SCRIPT_ID_ATTRIBUTE
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.SCRIPT_SETUP_JS_EMBEDDED_CONTENT
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.SCRIPT_SETUP_TS_EMBEDDED_CONTENT
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.SRC_ATTRIBUTE
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.STUBBED_ATTRIBUTE
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.STUBBED_TAG
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.TEMPLATE_TAG
import org.jetbrains.vuejs.lang.stubs.factories.VueJSEmbeddedExpressionContentStubFactory
import org.jetbrains.vuejs.lang.stubs.factories.VueJSScriptSetupTypeParameterListStubFactory
import org.jetbrains.vuejs.lang.stubs.factories.VueRefAttributeStubFactory
import org.jetbrains.vuejs.lang.stubs.factories.VueScriptIdAttributeStubFactory
import org.jetbrains.vuejs.lang.stubs.factories.VueScriptSetupEmbeddedContentStubFactory
import org.jetbrains.vuejs.lang.stubs.factories.VueStubBasedTagStubFactory
import org.jetbrains.vuejs.lang.stubs.factories.VueTemplateTagStubFactory
import org.jetbrains.vuejs.lang.stubs.serializers.VueFileStubSerializer
import org.jetbrains.vuejs.lang.stubs.serializers.VueJSEmbeddedExpressionContentStubSerializer
import org.jetbrains.vuejs.lang.stubs.serializers.VueJSScriptSetupTypeParameterListStubSerializer
import org.jetbrains.vuejs.lang.stubs.serializers.VueRefAttributeStubSerializer
import org.jetbrains.vuejs.lang.stubs.serializers.VueScriptIdAttributeStubSerializer
import org.jetbrains.vuejs.lang.stubs.serializers.VueScriptSetupEmbeddedContentStubSerializer
import org.jetbrains.vuejs.lang.stubs.serializers.VueSrcAttributeStubSerializer

class VueStubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    listOf(
      VueFileElementType.INSTANCE,
      VUEJS_FILE,
      VUETS_FILE,
    ).forEach {
      registry.registerStubSerializer(it, JSFileStubSerializer(it.language))
    }
    registry.registerStubSerializer(VueFileElementType.INSTANCE, VueFileStubSerializer())

    listOf(
      VueJSScriptSetupTypeParameterListStubFactory(),
      VueJSScriptSetupTypeParameterListStubSerializer(),

      VueJSEmbeddedExpressionContentStubFactory(EMBEDDED_EXPR_CONTENT_JS),
      VueJSEmbeddedExpressionContentStubSerializer(EMBEDDED_EXPR_CONTENT_JS),

      VueJSEmbeddedExpressionContentStubFactory(EMBEDDED_EXPR_CONTENT_TS),
      VueJSEmbeddedExpressionContentStubSerializer(EMBEDDED_EXPR_CONTENT_TS),

      VueScriptSetupEmbeddedContentStubFactory(SCRIPT_SETUP_TS_EMBEDDED_CONTENT),
      VueScriptSetupEmbeddedContentStubSerializer(SCRIPT_SETUP_TS_EMBEDDED_CONTENT),

      VueScriptSetupEmbeddedContentStubFactory(SCRIPT_SETUP_JS_EMBEDDED_CONTENT),
      VueScriptSetupEmbeddedContentStubSerializer(SCRIPT_SETUP_JS_EMBEDDED_CONTENT),
    ).forEach(registry::register)

    registry.registerStubFactory(STUBBED_TAG, VueStubBasedTagStubFactory(STUBBED_TAG))
    registry.registerStubSerializer(STUBBED_TAG, HtmlStubBasedTagStubSerializer(STUBBED_TAG))

    registry.registerStubFactory(TEMPLATE_TAG, VueTemplateTagStubFactory())
    registry.registerStubSerializer(TEMPLATE_TAG, HtmlStubBasedTagStubSerializer(TEMPLATE_TAG))

    registry.registerStubFactory(STUBBED_ATTRIBUTE, XmlStubBasedAttributeStubFactory(STUBBED_ATTRIBUTE))
    registry.registerStubSerializer(STUBBED_ATTRIBUTE, XmlStubBasedAttributeStubSerializer(STUBBED_ATTRIBUTE))

    registry.registerStubFactory(SCRIPT_ID_ATTRIBUTE, VueScriptIdAttributeStubFactory())
    registry.registerStubSerializer(SCRIPT_ID_ATTRIBUTE, VueScriptIdAttributeStubSerializer())

    registry.registerStubFactory(SRC_ATTRIBUTE, XmlStubBasedAttributeStubFactory(SRC_ATTRIBUTE))
    registry.registerStubSerializer(SRC_ATTRIBUTE, VueSrcAttributeStubSerializer())

    registry.registerStubFactory(REF_ATTRIBUTE, VueRefAttributeStubFactory())
    registry.registerStubSerializer(REF_ATTRIBUTE, VueRefAttributeStubSerializer())
  }
}