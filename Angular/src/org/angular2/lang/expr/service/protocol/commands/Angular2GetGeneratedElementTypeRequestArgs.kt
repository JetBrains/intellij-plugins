package org.angular2.lang.expr.service.protocol.commands

import com.intellij.lang.typescript.compiler.languageService.protocol.commands.Range
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetElementTypeRequestArgsBase
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptTypeRequestKind

data class Angular2GetGeneratedElementTypeRequestArgs(
  override val file: String,
  override val projectFileName: String?,
  val range: Range,
  override val forceReturnType: Boolean = false,
) : TypeScriptGetElementTypeRequestArgsBase {

  override val typeRequestKind: String
    get() = TypeScriptTypeRequestKind.Default.serialize()
}