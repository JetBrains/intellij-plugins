package org.angular2.lang.expr.service.protocol.commands

import com.intellij.lang.typescript.compiler.languageService.protocol.commands.Range
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetElementTypeRequestArgsBase

data class Angular2GetGeneratedElementTypeRequestArgs(
  override val file: String,
  override val projectFileName: String?,
  val startOffset: Int,
  val endOffset: Int,
  override val forceReturnType: Boolean = false,
) : TypeScriptGetElementTypeRequestArgsBase {

  override fun copyWithForceReturn(forceReturnType: Boolean): TypeScriptGetElementTypeRequestArgsBase =
    copy(forceReturnType = forceReturnType)

  override val range: Range?
    get() = null

}