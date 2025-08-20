// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.lang.javascript.modules.JSModuleNameInfo.ExtensionSettings
import com.intellij.lang.javascript.modules.imports.path.JSImportModulePathStrategy
import com.intellij.psi.PsiElement
import org.jetbrains.astro.lang.typescript.astroExtension
import org.jetbrains.astro.lang.typescript.astroExtensionsWithDot

class AstroImportModulePathStrategy : JSImportModulePathStrategy {
  override fun getDefaultImplicitExtensions(place: PsiElement): Array<String> = astroExtensionsWithDot

  override fun getPathSettings(place: PsiElement, extensionWithDot: String, auto: Boolean): ExtensionSettings? {
    return if (auto && extensionWithDot == astroExtension) ExtensionSettings.FORCE_EXTENSION else null
  }
}
