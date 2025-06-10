package com.jetbrains.plugins.jade.js

import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import com.jetbrains.plugins.jade.js.JavascriptInJadeParserDefinition.JS_IN_JADE_FILE

class JavaScriptInJadeStubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    JS_IN_JADE_FILE.let {
      registry.registerStubSerializer(it, JSFileStubSerializer(it.language))
    }
  }
}