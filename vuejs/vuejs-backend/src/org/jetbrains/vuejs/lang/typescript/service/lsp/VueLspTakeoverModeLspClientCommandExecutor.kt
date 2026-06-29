// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.google.gson.JsonElement
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.typescript.compiler.TypeScriptServiceQueueCommand
import com.intellij.lang.typescript.lsp.TypeScriptLspClientCommandExecutor
import com.intellij.platform.lsp.impl.LspClientImpl
import kotlinx.coroutines.CoroutineScope

internal class VueLspTakeoverModeLspClientCommandExecutor(client: LspClientImpl, commandCoroutineScope: CoroutineScope) :
  TypeScriptLspClientCommandExecutor(client, commandCoroutineScope) {

  override suspend fun handleCustomTsServerCommand(
    command: TypeScriptServiceQueueCommand<out JSLanguageServiceObject, out Any>,
    requiresNewEval: Boolean,
  ): JsonElement? =
    if (requiresNewEval)
      null
    else
      sendCustomTsServerCommand(command.command, command.arguments)

}
