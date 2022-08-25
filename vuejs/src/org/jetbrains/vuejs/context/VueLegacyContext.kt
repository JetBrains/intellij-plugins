// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.context

import com.intellij.javascript.web.context.WebFrameworkContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider

@Suppress("DEPRECATION")
internal class VueLegacyContext : WebFrameworkContext {
  override fun isEnabled(file: PsiFile): Boolean {
    return VueContextProvider.VUE_CONTEXT_PROVIDER_EP.extensionList.any {
      it.isVueContextEnabled(file)
    }
  }

  override fun isEnabled(directory: PsiDirectory): CachedValueProvider.Result<Int?> {
    val dependencies = mutableSetOf<Any>()
    for (provider in VueContextProvider.VUE_CONTEXT_PROVIDER_EP.extensionList) {
      val result = provider.isVueContext(directory)
      if (result.value == true) {
        return CachedValueProvider.Result(0, result.dependencyItems)
      }
      dependencies.addAll(result.dependencyItems)
    }
    if (dependencies.isEmpty()) {
      dependencies.add(ModificationTracker.NEVER_CHANGED)
    }
    return CachedValueProvider.Result(null, *dependencies.toTypedArray())
  }

  override fun isForbidden(contextFile: VirtualFile, project: Project): Boolean {
    return VueContextProvider.VUE_CONTEXT_PROVIDER_EP.extensionList.any {
      it.isVueContextForbidden(contextFile, project)
    }
  }
}