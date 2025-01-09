package org.angular2.lang.expr.service.protocol.commands

import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetElementTypeCommandBase

class Angular2GetGeneratedElementTypeCommand(args: Angular2GetGeneratedElementTypeRequestArgs)
  : TypeScriptGetElementTypeCommandBase<Angular2GetGeneratedElementTypeRequestArgs>("ngGetGeneratedElementType", args, null, null) {
  override fun copyWithForceReturn(): TypeScriptGetElementTypeCommandBase<Angular2GetGeneratedElementTypeRequestArgs> =
    Angular2GetGeneratedElementTypeCommand(arguments.copy(forceReturnType = true))
}
