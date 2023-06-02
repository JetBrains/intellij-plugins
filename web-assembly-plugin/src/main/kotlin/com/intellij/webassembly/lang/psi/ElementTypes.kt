package com.intellij.webassembly.lang.psi

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.tree.IElementType
import com.intellij.webassembly.lang.WebAssemblyLanguage
import com.intellij.webassembly.WebassemblyIcons
import javax.swing.Icon

class WebAssemblyTokenType(debugName: String) : IElementType(debugName, WebAssemblyLanguage) {
  override fun toString(): String = "WebAssemblyToken." + super.toString()
}

class WebAssemblyElementType(debugName: String) : IElementType(debugName, WebAssemblyLanguage)

object WebAssemblyFileType : LanguageFileType(WebAssemblyLanguage) {
  override fun getName(): String = "WebAssembly file"
  override fun getDescription(): String = "WebAssembly language file"
  override fun getDefaultExtension(): String = "wat"

  override fun getIcon(): Icon = WebassemblyIcons.WebAssemblyFiletype
}