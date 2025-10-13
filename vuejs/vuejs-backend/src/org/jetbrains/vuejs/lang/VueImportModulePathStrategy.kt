// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.modules.JSModuleNameInfo.ExtensionSettings
import com.intellij.lang.javascript.modules.imports.path.JSImportModulePathStrategy
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.index.VUE_DEFAULT_EXTENSIONS_WITH_DOT
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION

class VueImportModulePathStrategy : JSImportModulePathStrategy {
  override fun getPathSettings(place: PsiElement, extensionWithDot: String, auto: Boolean): ExtensionSettings? {
    //by default always use the explicit extension
    return if (auto && extensionWithDot == VUE_FILE_EXTENSION) ExtensionSettings.FORCE_EXTENSION else null
  }

  override fun getDefaultImplicitExtensions(place: PsiElement): Array<String> = VUE_DEFAULT_EXTENSIONS_WITH_DOT
}