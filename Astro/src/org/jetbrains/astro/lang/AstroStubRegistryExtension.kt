// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import org.jetbrains.astro.lang.frontmatter.AFM_FILE

class AstroStubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    AFM_FILE.let {
      registry.registerStubSerializer(it, JSFileStubSerializer(it.language))
    }
  }
}