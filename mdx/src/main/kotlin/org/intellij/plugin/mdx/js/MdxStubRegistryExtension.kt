package org.intellij.plugin.mdx.js

import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.psi.stubs.DefaultFileStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes

class MdxStubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    registry.registerStubSerializer(MdxElementTypes.MDX_FILE_NODE_TYPE, DefaultFileStubSerializer())
    registry.registerStubSerializer(FILE, JSFileStubSerializer(FILE))
  }
}
