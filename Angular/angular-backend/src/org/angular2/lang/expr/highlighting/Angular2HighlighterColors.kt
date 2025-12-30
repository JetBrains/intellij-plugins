// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.highlighting

import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.openapi.editor.colors.TextAttributesKey

interface Angular2HighlighterColors {
  companion object {
    @JvmField
    val NG_SIGNAL: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
      "NG.SIGNAL", TypeScriptHighlighter.TS_INSTANCE_MEMBER_FUNCTION)

    @JvmField
    val NG_VARIABLE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
      "NG.VARIABLE", TypeScriptHighlighter.TS_LOCAL_VARIABLE)
  }
}