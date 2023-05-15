package org.jetbrains.webstorm.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import org.jetbrains.webstorm.lang.WebAssemblyLanguage


class WebAssemblyFile(viewProvider: FileViewProvider?) : PsiFileBase(viewProvider!!, WebAssemblyLanguage) {
    override fun getFileType() = WebAssemblyFileType
    override fun toString() = "WebAssembly File"
}