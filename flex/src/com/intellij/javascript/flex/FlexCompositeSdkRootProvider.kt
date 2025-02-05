// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex

import com.intellij.lang.javascript.flex.projectStructure.FlexCompositeSdk
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.*
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.annotations.Unmodifiable

class FlexCompositeSdkRootProvider : AdditionalLibraryRootsProvider() {

  override fun getAdditionalProjectLibraries(project: Project): @Unmodifiable Collection<SyntheticLibrary> {
    if (!Registry.`is`("ide.workspace.model.sdk.remove.custom.processing")) return emptyList()

    val result = mutableListOf<SyntheticLibrary>()
    for (module in ModuleManager.getInstance(project).modules) {
      val rootManager = ModuleRootManager.getInstance(module)
      val sdk = rootManager.sdk
      if (sdk != null && sdk is FlexCompositeSdk) {
        val rootProvider = sdk.rootProvider
        val sourceRoots = rootProvider.getFiles(OrderRootType.SOURCES).toList()
        val binaryRoots = rootProvider.getFiles(OrderRootType.CLASSES).toList()
        result.add(SyntheticLibrary.newImmutableLibrary(sdk.name, sourceRoots, binaryRoots, emptySet(), null))
      }
    }
    return result
  }
}
