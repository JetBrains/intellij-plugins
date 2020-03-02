// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.CachedValueProvider

interface VueContextProvider {

  /**
   * All context providers are queried to determine whether files within a particular folder
   * should have Vue support enabled. Results are or'ed together. It is a responsibility of the
   * context provider to include all dependencies of the value. The result is being cached.
   *
   * It is important that result is stable as any change will result in full reload of code insight
   * and clear of all caches.
   */
  fun isVueContext(directory: PsiDirectory): CachedValueProvider.Result<Boolean>

  /**
   * Context providers can forbid Vue context on a particular file to allow for cooperation between different
   * plugins. This method is used before creating a PsiFile, so it should not try to use PsiManager to find a PsiFile.
   * The result is not cached and therefore the logic should not perform time consuming tasks, or should cache results
   * on it's own.
   *
   * It is important that result is stable as any change will result in full reload of code insight
   * and clear of all caches.
   */
  @JvmDefault
  fun isVueContextForbidden(context: VirtualFile, project: Project): Boolean = false

  companion object {
    val VUE_CONTEXT_PROVIDER_EP = ExtensionPointName.create<VueContextProvider>("com.intellij.vuejs.contextProvider")
  }
}
