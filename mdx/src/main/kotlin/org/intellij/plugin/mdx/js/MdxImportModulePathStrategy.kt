package org.intellij.plugin.mdx.js

import com.intellij.lang.javascript.modules.JSModuleNameInfo.ExtensionSettings
import com.intellij.lang.javascript.modules.imports.path.JSImportModulePathStrategy
import com.intellij.psi.PsiElement

class MdxImportModulePathStrategy : JSImportModulePathStrategy {

  private val extensions = arrayOf(".mdx")
  override fun getDefaultImplicitExtensions(): Array<String> = extensions

  override fun getPathSettings(place: PsiElement, extensionWithDot: String, auto: Boolean): ExtensionSettings? {
    return if (extensionWithDot == ".mdx") ExtensionSettings.FORCE_EXTENSION else null
  }
}