package com.intellij.deno.modules

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.modules.imports.path.JSImportModulePathStrategy
import com.intellij.psi.PsiElement

class DenoImportModulePathStrategy : JSImportModulePathStrategy {
  override fun getPathSettings(place: PsiElement,
                               extensionWithDot: String): JSModuleNameInfo.ExtensionSettings? {
    if (DialectDetector.isTypeScript(place)) {
      if (DenoSettings.getService(place.project).isUseDeno() && 
          extensionWithDot != ".d.ts") {
        return JSModuleNameInfo.ExtensionSettings.FORCE_EXTENSION
      }
    }

    return null
  }
}