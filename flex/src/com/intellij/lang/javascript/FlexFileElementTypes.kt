package com.intellij.lang.javascript

import com.intellij.lang.javascript.flex.FlexSupportLoader
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.tree.IFileElementType

object FlexFileElementTypes {
  @JvmField
  val ECMA4_FILE: IFileElementType =
    JSFileElementType.create(FlexSupportLoader.ECMA_SCRIPT_L4)
}
