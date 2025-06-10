package org.intellij.plugin.mdx.js

import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension

class MdxJSStubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    FILE.let {
      registry.registerStubSerializer(it, JSFileStubSerializer(it.language))
    }
  }
}