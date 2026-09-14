// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.typescript.lsp.JSExternalDefinitionsPackage
import com.intellij.lang.typescript.lsp.LspServerLoader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import java.util.concurrent.ConcurrentHashMap

@ApiStatus.Experimental
object VueLspServerHybridModeLoaderFactory {
  private val loaders = ConcurrentHashMap<VueServiceRuntime, Loader>()

  fun getLoader(runtime: VueServiceRuntime): LspServerLoader {
    return loaders.getOrPut(runtime) {
      createLoader(runtime)
    }
  }

  private fun createLoader(runtime: VueServiceRuntime): Loader {
    return Loader(VueLspServerPackageDescriptor(runtime.version.versionString))
  }
}

@ApiStatus.Experimental
private class Loader(
  private val descriptor: LspServerPackageDescriptor,
) : LspServerLoader(descriptor) {
  override fun getSelectedPackage(project: Project): NodePackage {
    return JSExternalDefinitionsPackage(descriptor.serverPackage)
  }
}
