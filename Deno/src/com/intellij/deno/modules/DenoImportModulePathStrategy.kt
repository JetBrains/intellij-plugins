package com.intellij.deno.modules

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.modules.imports.path.JSImportModulePathStrategy
import com.intellij.psi.PsiElement
import com.intellij.util.ArrayUtil

class DenoImportModulePathStrategy : JSImportModulePathStrategy {
  override fun getPathSettings(place: PsiElement,
                               extensionWithDot: String): JSModuleNameInfo.ExtensionSettings? {
    if (DenoSettings.getService(place.project).isUseDeno() &&
        !TypeScriptUtil.TYPESCRIPT_DECLARATIONS_FILE_EXTENSIONS.contains(extensionWithDot)) {
      return JSModuleNameInfo.ExtensionSettings.FORCE_EXTENSION
    }

    return null
  }
}