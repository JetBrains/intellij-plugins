// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.CachedValueProvider

interface VueContextProvider {

  fun isVueContext(directory: PsiDirectory): CachedValueProvider.Result<Boolean>

  companion object {
    val VUE_CONTEXT_PROVIDER_EP = ExtensionPointName.create<VueContextProvider>("com.intellij.vuejs.contextProvider")
  }
}
