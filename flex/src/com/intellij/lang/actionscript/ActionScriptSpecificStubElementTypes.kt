// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript

import com.intellij.lang.actionscript.types.ActionScriptLocalVariableElementType

/**
 * Part of [ActionScriptStubElementTypes] which can be moved to Flex plugin. Eventually, all types should be moved here.
 */
interface ActionScriptSpecificStubElementTypes {
  /**
   * Unique companion name required to avoid `StubElementTypeHolder` errors
   */
  companion object InternalActionScriptSpecificStubElementTypesCompanion {
    @JvmField
    val LOCAL_VARIABLE: ActionScriptLocalVariableElementType = ActionScriptLocalVariableElementType()
  }
}