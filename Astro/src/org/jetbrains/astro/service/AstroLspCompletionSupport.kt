// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.lsp.BaseLspTypeScriptServiceCompletionSupport
import org.eclipse.lsp4j.CompletionItem
import org.jetbrains.astro.AstroIcons
import javax.swing.Icon


internal class AstroLspCompletionSupport : BaseLspTypeScriptServiceCompletionSupport() {
  override fun getIcon(item: CompletionItem): Icon {
    if (isAstroComponent(item)) return AstroIcons.Astro

    return super.getIcon(item) ?: AstroIcons.Astro
  }

  private fun isAstroComponent(item: CompletionItem): Boolean {
    val isUpperCase = item.label.firstOrNull()?.isUpperCase() ?: return false
    if (!isUpperCase) return false

    val description = item.labelDetails?.description?.lowercase() ?: return false
    return description.contains("astro/components") || description.contains(".astro")
  }
}
