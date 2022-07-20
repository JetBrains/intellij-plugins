package com.intellij.deno.modules

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.modules.imports.path.JSImportModulePathStrategy
import com.intellij.psi.PsiElement

class DenoImportModulePathStrategy : JSImportModulePathStrategy {
  override fun getPathSettings(place: PsiElement,
                               extensionWithDot: String,
                               auto: Boolean): JSModuleNameInfo.ExtensionSettings? {
    if (!TypeScriptUtil.TYPESCRIPT_DECLARATIONS_FILE_EXTENSIONS.contains(extensionWithDot)) {
      return JSModuleNameInfo.ExtensionSettings.FORCE_EXTENSION
    }

    return null
  }

  override fun isAvailable(place: PsiElement): Boolean {
    return DenoSettings.getService(place.project).isUseDeno()
  }
}