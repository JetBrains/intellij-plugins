// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.lang.stubs

import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import org.angularjs.lang.parser.AngularJSParserDefinition

class AngularJSStubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    AngularJSParserDefinition.FILE.let {
      registry.registerStubSerializer(it, JSFileStubSerializer(it.language))
    }
  }
}