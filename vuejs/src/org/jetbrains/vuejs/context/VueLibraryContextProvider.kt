// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.lang.javascript.library.JSLibraryManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.CachedValueProvider

private class VueLibraryContextProvider : VueContextProvider {
  override fun isVueContext(directory: PsiDirectory): CachedValueProvider.Result<Boolean> =
    CachedValueProvider.Result(JSLibraryManager.getInstance(directory.project)
                                 .libraryMappings.isAssociatedWithProject("vue"),
                               ModificationTracker.NEVER_CHANGED)
}