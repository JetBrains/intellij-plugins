package com.intellij.webassembly.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.webassembly.lang.WebAssemblyLanguage


class WebAssemblyFile(viewProvider: FileViewProvider?) : PsiFileBase(viewProvider!!, WebAssemblyLanguage) {
  override fun getFileType() = WebAssemblyFileType
  override fun toString() = "WebAssembly File"
}