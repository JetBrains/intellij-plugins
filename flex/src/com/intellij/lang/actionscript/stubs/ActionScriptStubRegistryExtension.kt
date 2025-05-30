// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.stubs

import com.intellij.javascript.flex.compiled.DecompiledSwfParserDefinition
import com.intellij.lang.javascript.FlexFileElementTypes
import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension

class ActionScriptStubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    listOf(
      FlexFileElementTypes.ECMA4_FILE,
      DecompiledSwfParserDefinition.FILE_TYPE,
    ).forEach {
      registry.registerStubSerializer(it, JSFileStubSerializer(it.language))
    }
  }
}